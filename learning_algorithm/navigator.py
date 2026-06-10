import os
import osmnx as ox
import networkx as nx
import numpy as np
import pandas as pd
import joblib
from geopy.geocoders import Nominatim


class SafePathNavigator:

    def __init__(self):
        print("Loading road network...")
        self.G = ox.graph_from_place(
            "Bengaluru, India",
            network_type="walk"
        )

        print("Loading safety model...")
        self.model = joblib.load('safety_model.pkl')

        print("Attaching safety scores to road network...")
        self._attach_safety_scores()

        print("Navigator ready!")

    def _extract_features(self, lat, lng):
        idx = self._nearest_segment(lat, lng)
        row = self.features.iloc[idx]
        return np.array([
            row['streetlight_score'],
            row['cctv_score'],
            row['police_proximity'],
            row['bus_stop_score']
        ]).reshape(1, -1)

    def _nearest_segment(self, lat, lng):
        distances = np.sqrt(
            (self.features['centroid_lat'] - lat) ** 2 +
            (self.features['centroid_lng'] - lng) ** 2
        )
        return distances.idxmin()

    def _attach_safety_scores(self):

        # If already computed, load saved graph
        if os.path.exists('bengaluru_safety.graphml'):
            print("Loading precomputed safety graph...")
            self.G = ox.load_graphml('bengaluru_safety.graphml')
            print("Done!")
            return

        # Otherwise compute from scratch
        print("Loading precomputed features...")
        self.features = pd.read_csv('features_with_labels.csv')

        print("Computing safety scores for all edges...")
        count = 0
        for u, v, key, data in self.G.edges(data=True, keys=True):
            lat = (self.G.nodes[u]['y'] + self.G.nodes[v]['y']) / 2
            lng = (self.G.nodes[u]['x'] + self.G.nodes[v]['x']) / 2

            features = self._extract_features(lat, lng)
            score = float(self.model.predict(features)[0])
            score = np.clip(score, 0, 1)

            self.G[u][v][key]['safety_score'] = score
            count += 1

            if count % 50000 == 0:
                print(f"  Processed {count} edges...")

        print(f"Safety scores attached to {count} edges")

        # Save for future runs
        print("Saving graph with safety scores...")
        ox.save_graphml(self.G, 'bengaluru_safety.graphml')
        print("Saved!")

    def _score_path(self, path):
        scores = []
        for u, v in zip(path[:-1], path[1:]):
            score = self.G[u][v][0].get('safety_score', 0.5)
            scores.append(float(score))
        return float(np.mean(scores)) if scores else 0.5

    def _path_distance(self, path):
        return sum(
            float(self.G[u][v][0].get('length', 1))
            for u, v in zip(path[:-1], path[1:])
        )

    def _classify(self, score):
        if score >= 0.7:
            return "Safe"
        elif score >= 0.5:
            return "Moderate"
        else:
            return "Risky"

    def get_routes(self, origin_lat, origin_lng,
                   dest_lat, dest_lng, k=20, top_n=5):

        # Snap to nearest graph nodes
        origin_node = ox.nearest_nodes(
            self.G, origin_lng, origin_lat
        )
        dest_node = ox.nearest_nodes(
            self.G, dest_lng, dest_lat
        )

        print(f"Finding {k} diverse paths...")

        # Convert to simple graph
        G_simple = nx.Graph(self.G)

        # Copy safety scores and length to simple graph
        for u, v, data in self.G.edges(data=True):
            if G_simple.has_edge(u, v):
                if 'safety_score' not in G_simple[u][v]:
                    G_simple[u][v]['safety_score'] = data.get(
                        'safety_score', 0.5
                    )
                if 'length' not in G_simple[u][v]:
                    G_simple[u][v]['length'] = data.get('length', 1)

        # Get shortest path as baseline
        try:
            baseline = nx.shortest_path(
                G_simple, origin_node, dest_node, weight='length'
            )
        except nx.NetworkXNoPath:
            print("No path found between these locations")
            return []

        shortest_distance = self._path_distance(baseline)
        max_allowed = shortest_distance * 1.5

        candidate_paths = [baseline]

        # Find diverse paths by penalising already used edges
        G_penalized = G_simple.copy()

        for _ in range(k - 1):
            used_edges = set()
            for path in candidate_paths:
                for u, v in zip(path[:-1], path[1:]):
                    used_edges.add((u, v))
                    used_edges.add((v, u))

            for u, v in used_edges:
                if G_penalized.has_edge(u, v):
                    G_penalized[u][v]['length'] = (
                        G_penalized[u][v].get('length', 1) * 3
                    )

            try:
                new_path = nx.shortest_path(
                    G_penalized, origin_node,
                    dest_node, weight='length'
                )
                path_dist = self._path_distance(new_path)
                if path_dist <= max_allowed:
                    candidate_paths.append(new_path)
            except nx.NetworkXNoPath:
                break

        # Score each path
        scored_routes = []
        for path in candidate_paths:
            distance = self._path_distance(path)
            safety = self._score_path(path)
            time_min = (distance / 1000) / 5 * 60

            scored_routes.append({
                'path': path,
                'distance_m': round(distance),
                'estimated_time_min': round(time_min, 1),
                'safety_score': round(safety, 3),
                'safety_class': self._classify(safety),
                'extra_distance_pct': round(
                    (distance / shortest_distance - 1) * 100, 1
                )
            })

        # Rank by safety score
        scored_routes.sort(
            key=lambda x: x['safety_score'],
            reverse=True
        )

        # Keep only top_n safest
        scored_routes = scored_routes[:top_n]

        for i, route in enumerate(scored_routes):
            route['rank'] = i + 1

        return scored_routes

    def print_routes(self, routes):
        print("\n" + "=" * 60)
        print("SAFEPATH AI — ROUTE RECOMMENDATIONS")
        print("=" * 60)

        for route in routes:
            print(f"\nRank {route['rank']} — {route['safety_class']}")
            print(f"  Safety Score:   {route['safety_score']:.3f}")
            print(f"  Distance:       {route['distance_m']}m")
            print(f"  Est. Time:      {route['estimated_time_min']} min")
            print(f"  Extra Distance: +{route['extra_distance_pct']}%")

        print("\n" + "=" * 60)
        safest = routes[0]
        fastest = min(routes, key=lambda x: x['distance_m'])

        if safest['rank'] != fastest['rank']:
            print(f"Recommendation: Take Rank {safest['rank']} route")
            diff = safest['safety_score'] - fastest['safety_score']
            print(
                f"  {safest['extra_distance_pct']}% longer "
                f"but {diff:.3f} safer"
            )
        else:
            print("The safest route is also the shortest!")

    def visualise_routes(self, routes):
        try:
            import folium
            m = folium.Map(
                location=[12.97, 77.59],
                zoom_start=13
            )

            colors = ['green', 'blue', 'orange', 'red', 'purple']

            for i, route in enumerate(routes):
                coords = [
                    (self.G.nodes[n]['y'], self.G.nodes[n]['x'])
                    for n in route['path']
                ]
                folium.PolyLine(
                    coords,
                    color=colors[i % len(colors)],
                    weight=4,
                    tooltip=(
                        f"Rank {route['rank']} | "
                        f"Safety: {route['safety_score']} | "
                        f"{route['safety_class']} | "
                        f"{route['distance_m']}m"
                    )
                ).add_to(m)

            m.save('routes_map.html')
            print("Map saved to routes_map.html — open in browser!")

        except ImportError:
            print("Install folium: pip install folium")


def get_coordinates(place_name):
    geolocator = Nominatim(user_agent="safepath_ai")
    try:
        location = geolocator.geocode(
            f"{place_name}, Bengaluru, India"
        )
        if location:
            return location.latitude, location.longitude
        else:
            print(f"Could not find: {place_name}")
            return None, None
    except Exception as e:
        print(f"Geocoding error: {e}")
        return None, None


# ── Main ───────────────────────────────────────────────────────
if __name__ == "__main__":

    navigator = SafePathNavigator()

    print("\nSafePath AI — Women's Safety Navigation")
    print("=" * 40)

    origin_name = input("Enter origin (e.g. Koramangala): ")
    dest_name = input("Enter destination (e.g. Indiranagar): ")

    origin_lat, origin_lng = get_coordinates(origin_name)
    dest_lat, dest_lng = get_coordinates(dest_name)

    if origin_lat and dest_lat:
        routes = navigator.get_routes(
            origin_lat=origin_lat,
            origin_lng=origin_lng,
            dest_lat=dest_lat,
            dest_lng=dest_lng,
            k=20,
            top_n=5
        )

        navigator.print_routes(routes)
        navigator.visualise_routes(routes)

    else:
        print("Could not find one or both locations. Please try again.")
