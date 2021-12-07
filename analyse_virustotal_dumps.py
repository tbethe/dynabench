import json

file_name = 'results/<result_folder>/virustotal.json'

with open(file_name) as f:
   data = json.load(f)

malicious_found = False

for report in data:
    stats = report['data']['attributes']['stats']
    for stat in stats:
        if stat == 'malicious' and stats[stat] != 0:
            print(f'!!!! {stat}: {stats[stat]}')
            malicious_found = True
        else:
            print(f'{stat}: {stats[stat]}')
    print('-----')
        
print(f'Malicious found: {malicious_found}')
