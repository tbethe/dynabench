#!/usr/bin/env python3

import requests
import sys
import time
import json
from pathlib import Path

from datetime import datetime

# Submit to provider

class APK_Submitter:
    
    def __init__(self, apk_path_list, result_directory):
        self.analysers = {}
        self.apk_path_list = apk_path_list
        self.result_directory = result_directory

    def register_analyser(self, name, function):
        print(f"Registered {name}")
        self.analysers[name] = function

    def submit(self):
        for analyser, function in self.analysers.items():
            print('-'*20)
            print(f"Submitting to {analyser}")
            print('-'*20)
            function(self.apk_path_list, self.result_directory)
        print('-'*20)
        print("FINISHED SUBMITTING")
        print('-'*20)




##############################
# Analyser
##############################

def virus_total(apks, dir):
    api_key = "375ff21a5f221fe5d201d6d8661e2e40d3cc5121996d87f8722baa1c9d57efdb"
    key_header = { "x-apikey": api_key }
    # Public API only allows 4 requests per minute. So we submit in batches.
    start_time_last_batch = datetime.now()


    for i, apk in enumerate(apks):
        with open(f'{dir}/virustotal.json', 'a') as f:

            print(f'Submitting {apk}')
            if i % 4 == 3:

                elapsed_time_since_start_of_batch = abs((start_time_last_batch - datetime.now()).total_seconds())
                if elapsed_time_since_start_of_batch < 240:
                    sleep_time = 250 - elapsed_time_since_start_of_batch
                    print(f'Sleeping for {sleep_time} seconds, to avoid going over VirusTotal limit ...', end='', flush=True)
                    time.sleep(sleep_time)
                    print(' continuing')
                start_time_last_batch = datetime.now()

            # submit to VirusTotal 

            with open(apk, 'rb') as file:
                submit_respons = requests.post(
                    "https://www.virustotal.com/api/v3/files",
                    headers = key_header,
                    files = { 'file': (file.name, file.read()) },
                )

            print('Waiting for result to be ready', end='', flush=True)
            
            response_json = submit_respons.json()
            id = response_json['data']['id']
            # Fetch result, check if it's completed, if not try again
            
            r = requests.get(
                f'https://www.virustotal.com/api/v3/analyses/{id}',
                headers = key_header
            )    
            while r.json()['data']['attributes']['status'] == 'queued':
                time.sleep(60)
                r = requests.get(
                    f'https://www.virustotal.com/api/v3/analyses/{id}',
                    headers = key_header
                )
                print('.', end='')
                
            print(' Done!\n', flush=True)
            f.write(json.dumps(r.json(), indent=4))
            f.write('\n')

            
#############################
# Main
#############################

def main():
    result_directory = sys.argv[1]
    apks_file_paths = sys.argv[2:]
    if (len(apks_file_paths) == 0):
            print(f"USAGE: python3 {__file__} result_directory [list of apks]")
            exit(1)

    result_path = Path(result_directory)
    if not result_path.is_dir():
        print(
            f"USAGE: python3 {__file__} result_directory [list of apks]\n Result directory must exist." \
            f"Args: {sys.argv}"
        )
        exit(1)
    elif list(result_path.iterdir()):
        print(
            f"USAGE: python3 {__file__} result_directory [list of apks]\n result_directory must be empty." \
            f"Args: {sys.argv[1:]}"
        )
        exit(1)

    submitter = APK_Submitter(
        apks_file_paths,
        result_directory,
    )

    # Register analysers
    submitter.register_analyser("VirusTotal", virus_total)
    
    submitter.submit()

if __name__ == "__main__":
    main()
