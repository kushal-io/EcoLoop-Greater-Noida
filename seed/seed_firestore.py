#!/usr/bin/env python3
"""
EcoLoop Greater Noida - Firestore Seeder Script
Uploads realistic partners and waste category records to Cloud Firestore.

Usage:
    1. Generate a Service Account Key from Firebase Console:
       Project Settings -> Service accounts -> Generate new private key -> save as serviceAccountKey.json in this directory.
    2. Install dependency:
       pip install firebase-admin
    3. Run script:
       python seed_firestore.py
"""

import json
import os
import sys

try:
    import firebase_admin
    from firebase_admin import credentials, firestore
except ImportError:
    print("Error: 'firebase-admin' package is required.")
    print("Install it with: pip install firebase-admin")
    sys.exit(1)

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
SERVICE_ACCOUNT_KEY_PATH = os.path.join(SCRIPT_DIR, "serviceAccountKey.json")
PARTNERS_JSON_PATH = os.path.join(SCRIPT_DIR, "seed_partners.json")
CATEGORIES_JSON_PATH = os.path.join(SCRIPT_DIR, "seed_categories.json")
REWARDS_JSON_PATH = os.path.join(SCRIPT_DIR, "seed_rewards.json")

def seed():
    if not os.path.exists(SERVICE_ACCOUNT_KEY_PATH):
        print(f"Service account key not found at: {SERVICE_ACCOUNT_KEY_PATH}")
        print("Please download serviceAccountKey.json from Firebase Console -> Project Settings -> Service accounts.")
        sys.exit(1)

    cred = credentials.Certificate(SERVICE_ACCOUNT_KEY_PATH)
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    print("[*] Seeding Waste Categories...")
    with open(CATEGORIES_JSON_PATH, "r", encoding="utf-8") as f:
        categories = json.load(f)
        for cat in categories:
            cat_id = cat.get("id")
            db.collection("waste_categories").document(cat_id).set(cat)
            print(f"  + Category '{cat_id}' uploaded.")

    print("\n[*] Seeding 15 Greater Noida Recycling Partners...")
    with open(PARTNERS_JSON_PATH, "r", encoding="utf-8") as f:
        partners = json.load(f)
        for partner in partners:
            partner_id = partner.get("id")
            db.collection("partners").document(partner_id).set(partner)
            print(f"  + Partner '{partner['name']}' ({partner_id}) uploaded.")

    print("\n[*] Seeding 5 Greater Noida Reward Items...")
    with open(REWARDS_JSON_PATH, "r", encoding="utf-8") as f:
        rewards = json.load(f)
        for reward in rewards:
            rew_id = reward.get("id")
            db.collection("rewards").document(rew_id).set(reward)
            print(f"  + Reward '{reward['title']}' ({rew_id}) uploaded.")

    print("\n[SUCCESS] All Greater Noida partners, categories, and rewards seeded successfully into Firestore!")

if __name__ == "__main__":
    seed()
