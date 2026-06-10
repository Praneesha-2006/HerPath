import pandas as pd
import numpy as np

print("+"*50)
print("BBMP WARD DATA")
print("="*50)

bbmp=pd.read_csv('features.csv')
print(f"shape: {bbmp.shape}")
print(f"colums:{bbmp.columns.tolist()}")
print("\nFirst 3 rows:")
print(bbmp.head(3))
print(f"\nMissing values:")
print(bbmp.isnull().sum())

#safecity incidents
print("\n" + "=" *50)
print("SAFECITY INCIDENTS")
print("="*50)

safecity=pd.read_csv('bengaluru_safecity.csv')
print(f"shape:{safecity.shape}")
print(f"columns: {safecity.columns.tolist()}")
print(f"\nFirst 3 rows")
print(safecity.head(3))

#convert to float
safecity['latitude']=pd.to_numeric(safecity['latitude'],errors='coerce')
safecity['longitude']=pd.to_numeric(safecity['longitude'],errors='coerce')

#filter for bengaluru
bengaluru_incidents=safecity[(safecity['latitude']>=12.834)&(safecity['latitude']<=13.139)&(safecity['longitude']>=77.460)&(safecity['longitude'
]<=77.780)]

print(f"total incidents:{len(safecity)}")
print(f"bengaluru incidents:{len(bengaluru_incidents)}")
print(f"\ncoordinate ranges:")
print(f"lat:{safecity['latitude'].min():.4f} to {safecity['latitude'].max():.4f}")
print(f"lng: {safecity['longitude'].min():.4f} to {safecity['longitude'].max():.4f}")

