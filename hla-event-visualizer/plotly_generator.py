"""
This script generates a (slightly modified) Gantt chart of CONSERT's knowledge base facts.
It reads the JSON exports from CONSERT and produces an html Gantt chart using Plotly (https://plot.ly/python/). 
"""

import os
import argparse
import json

import pandas as pd
from pandas.io.json import json_normalize

import plotly
import plotly.figure_factory as ff

__author__ = "Mihai Trascau"
__maintainer__ = "Mihai Trascau"
__date__ = "23/05/2017"
__version__ = "1.0.0"

# Parse arguments (input folder and output file)
parser = argparse.ArgumentParser(description='Visualization script built with Plotly for CONSERT event processing')
parser.add_argument('--f', metavar='INPUT_FOLDER', type=str, nargs=1, required=True,
                    help='folder containin  g the CONSERT knowledgebase exported in JSON files (usually one per each'
                         'entrypoint')
parser.add_argument('--o', metavar='OUTPUT_HTML', type=str, nargs=1, default=['hla-kb-visualizer.html'],
                    help='output html file containing the plot')
args = parser.parse_args()
INPUT_FOLDER = args.f[0]
OUTPUT_HTML = args.o[0]

# Filter all JSON files (ending with .json) in INPUT_FOLDER
files = [os.path.join(INPUT_FOLDER, f) for f in os.listdir(INPUT_FOLDER) if f.endswith('.json')]

# Collect all data from JSON files into a single pandas dataframe
all_frames = []
for f in files:
    with open(f, 'r') as data_file:
        key = ''
        if f.lower().find('pos') != -1:
            key = 'POS'
        elif f.lower().find('lla') != -1:
            key = 'LLA'
        elif f.lower().find('hla') != -1:
            key = 'HLA'
        else:
            # print 'Skipping JSON file \'' + f + '\''
            continue
        raw_data = json.load(data_file)
        partial_frame = json_normalize(raw_data)
        partial_frame['Event Type'] = key
        all_frames.append(partial_frame)
df = pd.concat(all_frames)

# Keep only the needed columns
df = df[['type.type', 'annotations.startTime', 'annotations.endTime', 'Event Type']]

# Convert annotations to datetime from UNIX timestamps (dtype int64)
df['annotations.startTime'] = pd.to_datetime(df['annotations.startTime'], unit='ms')
df['annotations.endTime'] = pd.to_datetime(df['annotations.endTime'], unit='ms')

# Rename columns to match plotly conventions
df.rename(columns={'type.type': 'Task', 'annotations.startTime': 'Start', 'annotations.endTime': 'Finish'}, inplace=True)

# Sort dataframe so we get HLAs in the lower part of the Gantt
df.sort_values(by="Event Type", ascending=False, inplace=True)
df.reset_index(inplace=True)

# Set color map for different event types
colors = {'POS': 'rgb(211, 211, 211)', 'LLA': 'rgb(169, 169, 169)', 'HLA': 'rgb(0, 220, 0)'}

# Create Gantt figure
fig = ff.create_gantt(df, colors=colors, index_col='Event Type', title='HLA Recognition Scenario',
                      showgrid_y=True, show_colorbar=True, group_tasks=True)

# Tilt y labels 45 degrees (due to them being too long to display correctly)
fig['layout']['yaxis']['tickangle'] = 45

# Plot the figure in offline mode (this creates and opens the resulting HTML file)
plotly.offline.plot(fig, filename=OUTPUT_HTML)
