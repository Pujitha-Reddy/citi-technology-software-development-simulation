
import re
import pandas as pd
import matplotlib.pyplot as plt

# Java output from the market data application
java_output = """
Added data point: price=508.48999, timestamp=2026-09-30T23:16:02.725560053Z
Added data point: price=508.48999, timestamp=2026-09-30T23:16:18.136390883Z
Added data point: price=508.48999, timestamp=2026-09-30T23:16:33.580289197Z
Added data point: price=508.48999, timestamp=2026-09-30T23:16:49.008882314Z
Added data point: price=508.48999, timestamp=2026-09-30T23:17:04.462450833Z
Added data point: price=508.48999, timestamp=2026-09-30T23:17:19.884750633Z
Added data point: price=508.48999, timestamp=2026-09-30T23:17:35.305826229Z
"""

# Extract price and timestamp
matches = re.findall(
    r"price=([0-9.]+), timestamp=([^\n]+)",
    java_output
)

# Convert data into a DataFrame
df = pd.DataFrame(matches, columns=["price", "timestamp"])

# Convert data types
df["price"] = df["price"].astype(float)
df["timestamp"] = pd.to_datetime(df["timestamp"])

print(df)

# Create the line graph
plt.figure(figsize=(10, 5))
plt.plot(df["timestamp"], df["price"], marker="o")

plt.title("DIA Price Over Time")
plt.xlabel("Timestamp")
plt.ylabel("Price")

plt.xticks(rotation=45)
plt.tight_layout()
plt.show()
