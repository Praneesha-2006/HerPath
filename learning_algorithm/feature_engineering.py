import pandas as pd
import numpy as np
from sklearn.preprocessing import MinMaxScaler

#load bbmp data
bbmp=pd.read_csv('features.csv')

#select relevant features
features=bbmp[['Ward Name','Streetlights #','Police Stations #','Bus Stops #','Population density 2011 (persons / sq km)','Area (sq km)']].copy()

#--clean column names--
features.columns=['ward-name','streetlights','police_stations','bus_stops','population_density','area_sqkm']

#convert to numeric
for col in ['streetlights','police_stations','bus_stops','population_density','area_sqkm']:
	features[col]=pd.to_numeric(features[col],errors='coerce')

#fill missing values with median
features.fillna(features.median(numeric_only=True),inplace=True)

#normalise by area(density per sq km)
features['streetlight_density']=(features['streetlights']/features['area_sqkm'])
features['bus_stop_density']=(features['bus_stops']/features['area_sqkm'])

#police stations- keep as count
#population density - already in sq km

#drop raw columns
features.drop(columns=['streetlights','bus_stops','area_sqkm'],inplace=True)

print("features after engineering:")
print(features.head())
print(f"\nShape:{features.shape}")
print(f"\nStats:")
print(features.describe())

#normalize all features 0-1
feature_cols=['streetlight_density','police_stations','bus_stop_density','population_density']

scalar=MinMaxScaler()
features[feature_cols]=scalar.fit_transform(features[feature_cols])

print("\nNormalised features:")
print(features[feature_cols].describe())

#save
features.to_csv('ward_features.csv',index=False)
print("\nSaved to ward_features.csv")
