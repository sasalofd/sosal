import os
import zipfile
import json
from PIL import Image, ImageEnhance, ImageDraw
import random

# Paths
jar_path = r"C:\Users\salo2\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar"
assets_item_dir = r"src\main\resources\assets\beer\textures\item"
assets_entity_dir = r"src\main\resources\assets\beer\textures\entity\fish"
models_item_dir = r"src\main\resources\assets\beer\models\item"

os.makedirs(assets_item_dir, exist_ok=True)
os.makedirs(assets_entity_dir, exist_ok=True)
os.makedirs(models_item_dir, exist_ok=True)

fishes = {
    "cod": {"item": "cod.png", "entity": "cod.png"},
    "salmon": {"item": "salmon.png", "entity": "salmon.png"},
    "pufferfish": {"item": "pufferfish.png", "entity": "pufferfish.png"},
    "tropical_fish": {"item": "tropical_fish.png", "entity": "tropical_a.png"}
}

def process_image(img, mode):
    img = img.convert("RGBA")
    data = img.getdata()
    new_data = []
    
    for item in data:
        if item[3] == 0:
            new_data.append(item)
            continue
            
        r, g, b, a = item
        
        if mode == "salted":
            # "Забеленная, типа в соли" (whitened, covered in salt)
            # Add white salt specs
            if random.random() < 0.3:
                r = min(255, r + 150)
                g = min(255, g + 150)
                b = min(255, b + 150)
            else:
                r = min(255, r + 80)
                g = min(255, g + 80)
                b = min(255, b + 80)
            new_data.append((r, g, b, a))
            
        elif mode == "dried":
            # "Соленая, чуть темнее чем обычная" (salted/dried, slightly darker)
            r = int(r * 0.75)
            g = int(g * 0.65)
            b = int(b * 0.6)
            new_data.append((r, g, b, a))
            
    img.putdata(new_data)
    return img

try:
    with zipfile.ZipFile(jar_path, 'r') as z:
        for fish_name, info in fishes.items():
            # Item textures
            item_entry = f"assets/minecraft/textures/item/{info['item']}"
            with z.open(item_entry) as f:
                img = Image.open(f)
                
                # Salted item
                salted_img = process_image(img, "salted")
                salted_img.save(os.path.join(assets_item_dir, f"salted_{fish_name}.png"))
                
                # Dried item
                dried_img = process_image(img, "dried")
                dried_img.save(os.path.join(assets_item_dir, f"dried_{fish_name}.png"))
                
                # Item Models
                salted_model = { "parent": "minecraft:item/generated", "textures": { "layer0": f"beer:item/salted_{fish_name}" } }
                with open(os.path.join(models_item_dir, f"salted_{fish_name}.json"), "w") as mf:
                    json.dump(salted_model, mf, indent=2)
                    
                dried_model = { "parent": "minecraft:item/generated", "textures": { "layer0": f"beer:item/dried_{fish_name}" } }
                with open(os.path.join(models_item_dir, f"dried_{fish_name}.json"), "w") as mf:
                    json.dump(dried_model, mf, indent=2)

            # Entity textures
            entity_entry = f"assets/minecraft/textures/entity/fish/{info['entity']}"
            with z.open(entity_entry) as f:
                img = Image.open(f)
                
                # Salted entity
                salted_img = process_image(img, "salted")
                salted_img.save(os.path.join(assets_entity_dir, f"salted_{info['entity']}"))
                
                # Dried entity
                dried_img = process_image(img, "dried")
                dried_img.save(os.path.join(assets_entity_dir, f"dried_{info['entity']}"))

    print("Textures and models generated successfully.")
except Exception as e:
    print("Error:", e)
