#!/usr/bin/env python3

import redis
import os

def migrate_sets(source_host='localhost', source_port=6379,
                 dest_host='', dest_port=11254,
                 dest_password='',
                 sets_to_migrate=['animals', 'countries', 'cars']):

    if dest_host == '' or dest_password == '':
        raise Exception('Please provide a valid host and a password for destination')
    
    # Connect to source Redis (local)
    source = redis.Redis(host=source_host, port=source_port)
    
    # Connect to destination Redis (Upstash)
    dest = redis.Redis(
        host=dest_host,
        port=dest_port,
        decode_responses=True,
        username='default',
        password=dest_password,
    )
    
    # Migrate each set
    for set_name in sets_to_migrate:
        members = source.smembers(set_name)
        if members:
            # Use pipeline for better performance
            pipe = dest.pipeline()
            pipe.delete(set_name)  # Clear existing set if any
            pipe.sadd(set_name, *members)
            pipe.execute()
            print(f"Migrated {set_name} with {len(members)} members")
        else:
            print(f"Set {set_name} not found or empty")

if __name__ == "__main__":
    # Get Upstash credentials from environment variables
    redis_host = os.getenv('REDIS_HOST')
    redis_password = os.getenv('REDIS_PASSWORD')
    
    print('values are:')
    print(redis_host, redis_password)
    migrate_sets(
        dest_host=redis_host,
        dest_password=redis_password
    )
