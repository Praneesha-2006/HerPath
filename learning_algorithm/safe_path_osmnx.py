import osmnx as ox
import geopandas as gpd
import numpy as np
import pandas as pd
from scipy.spatial import KDTree

print("Downloading Bengaluru road network...")
G=ox.graph_from_place("Bengaluru,India",network_type="walk")

nodes,edges=ox.graph_to_gdfs(G)
print(f"Total road segments: {len(edges)}")

#Get centroid of each road segment
edges['centroid_lat']=edges.geometry.centroid.y
edges['centroid_lng']=edges.geometry.centroid.x
coords=edges[['centroid_lat','centroid_lng']].values

#download POIs
print("Downloading street lamps...")
lamps=ox.features_from_place("Bengaluru,India",tags={"highway":"street_lamp"})
lamp_coords=np.array([[g.centroid.y,g.centroid.x]for g in lamps.geometry])
print(f"street lamps found:{len(lamp_coords)}")

print("downloading cctv...")
cctv=ox.features_from_place("Bengaluru,India",tags={"man_made":"surveillance"})
cctv_coords=np.array([[g.centroid.y,g.centroid.x]for g in cctv.geometry])
print(f"CCTV found: {len(cctv_coords)}")

print("Downloading police stations...")
police=ox.features_from_place("Bengaluru,India",tags={"amenity":"police"})
police_coords=np.array([[g.centroid.y,g.centroid.x]for g in police.geometry])
print(f"Police stations found: {len(police_coords)}")

print("Downloading bus stops...")
bus=ox.features_from_place("Bengaluru,India",tags={"highway":"bus_stop"})
bus_coords=np.array([[g.centroid.y,g.centroid.x] for g in bus.geometry])
print(f"bus stops found:{len(bus_coords)}")

#build KDTrees
lamp_tree=KDTree(lamp_coords)
cctv_tree=KDTree(cctv_coords)
police_tree=KDTree(police_coords)
bus_tree=KDTree(bus_coords)

#extract features per segment
print("computing features per road segment...")
radius=0.002 #200 m approx
def count_within_radius(tree,point,radius):
	return len(tree.query_ball_point(point,radius))
def distance_to_nearest(tree,point):
	dist,_=tree.query(point)
	return dist*111		#convert degrees to km

features=[]
for i, (lat,lng) in enumerate(coords):
	if i%10000==0:
		print(f"processing{i}/{len(coords)}...")
	point=[lat,lng]
	features.append({'streetlight_score':count_within_radius(lamp_tree,point,radius),'cctv_score':count_within_radius(cctv_tree,point,radius),'police_distance_km':distance_to_nearest(police_tree,point),'bus_stop_score':count_within_radius(bus_tree,point,radius)})
feature_df=pd.DataFrame(features)

#invert polic distance-  closer is safer
from sklearn.preprocessing import MinMaxScaler

feature_df['police_proximity']=(1/(1+feature_df['police_distance_km']))
feature_df.drop('police_distance_km',axis=1,inplace=True)

#normalise to 0-1
scaler=MinMaxScaler()
feature_df=pd.DataFrame(scaler.fit_transform(feature_df),columns=feature_df.columns)

#attach centroid coordinates for spatial join later
feature_df['centroid_lat']=coords[:,0]
feature_df['centroid_lng']=coords[:,1]

print(f"\nFeature matrix shape:{feature_df.shape}")
print(feature_df.describe())

#save
feature_df.to_csv('street_features.csv',index=False)
print("\nSaved to street_features.csv")
