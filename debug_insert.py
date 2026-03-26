import ast

with open(r'c:\Users\Pragna Sree\AndroidStudioProjects\SmileAI\backend\app.py', 'r', encoding='utf-8') as f:
    content = f.read()

# Extract the query
import re
query_match = re.search(r'query = """(INSERT INTO cases \(.*?\)) VALUES \(.*?\)\"""', content, re.DOTALL)
if query_match:
    query = query_match.group(1)
    cols_part = re.search(r'\((.*?)\)', query).group(1)
    cols = [c.strip().strip('`') for c in cols_part.split(',')]
    print(f"Query Columns ({len(cols)}):")
    for i, col in enumerate(cols):
        print(f"{i+1}: {col}")

# Extract values tuple
values_match = re.search(r'values = \((.*?)\)\n', content, re.DOTALL)
if values_match:
    vals_str = values_match.group(1)
    # This is a bit tricky due to functions in the tuple, but let's try a simple split by comma
    # or just look at it manually which I did, but I want to be 100% sure.
    print("\nValues Str (manual check needed, but extracted):")
    print(vals_str)
