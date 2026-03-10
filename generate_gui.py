from PIL import Image, ImageDraw, ImageEnhance
import os

def create_malt_vat_gui():
    # Стандартный размер 256x256, рабочая область 176x166
    width, height = 256, 256
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 1. Фон (серый)
    bg_color = (198, 198, 198, 255)
    draw.rectangle([0, 0, 175, 165], fill=bg_color, outline=(0, 0, 0, 255))
    
    # Заголовок
    # draw.text((8, 6), "Malt Vat", fill=(64, 64, 64, 255))
    
    # 2. Слоты (стандартные 18x18)
    def draw_slot(x, y):
        # Внешняя рамка (темная)
        draw.rectangle([x, y, x+17, y+17], fill=(139, 139, 139, 255), outline=(55, 55, 55, 255))
        # Внутренняя подсветка
        draw.line([x+1, y+17, x+17, y+17], fill=(255, 255, 255, 255))
        draw.line([x+17, y+1, x+17, y+17], fill=(255, 255, 255, 255))

    # Слот 0: Дробленый солод (80, 15)
    draw_slot(79, 14)
    # Слот 1: Ведро воды (20, 55)
    draw_slot(19, 54)
    # Слот 2: Пустое ведро (55, 55)
    draw_slot(54, 54)
    # Слот 3: Результат (110, 55)
    draw_slot(109, 54)
    
    # 3. Стрелка прогресса (на текстуре 176, 0)
    # Фоновая стрелка
    draw.rectangle([176, 0, 176+23, 16], fill=(139, 139, 139, 255), outline=(100, 100, 100, 255))
    # Заполненная стрелка (часть ее будет рисоваться кодом)
    # Но для текстуры мы нарисуем контур
    
    # 4. Резервуары (Вертикальные полоски)
    # Вода (40, 20)
    draw.rectangle([39, 19, 39+11, 19+51], fill=(100, 100, 100, 255), outline=(55, 55, 55, 255))
    # Сусло (126, 20)
    draw.rectangle([125, 19, 125+11, 19+51], fill=(100, 100, 100, 255), outline=(55, 55, 55, 255))

    os.makedirs("src/main/resources/assets/beer/textures/gui", exist_ok=True)
    img.save("src/main/resources/assets/beer/textures/gui/malt_vat.png")
    print("Malt Vat GUI created.")

def create_beer_textures():
    base_path = "src/main/resources/assets/beer/textures/block/default_beer.png"
    if not os.path.exists(base_path):
        print(f"Base texture {base_path} not found!")
        return

    base_img = Image.open(base_path).convert("RGBA")
    
    # 1. Сидр (более светлый, желто-зеленый оттенок)
    cider_img = base_img.copy()
    data = cider_img.getdata()
    new_data = []
    for item in data:
        if item[3] > 0:
            r, g, b, a = item
            # Сдвигаем в сторону желтого/зеленого
            r = min(255, int(r * 1.1))
            g = min(255, int(g * 1.2))
            b = int(b * 0.7)
            new_data.append((r, g, b, a))
        else:
            new_data.append(item)
    cider_img.putdata(new_data)
    cider_img.save("src/main/resources/assets/beer/textures/block/cider.png")
    print("Cider texture created.")

    # 2. Ячменное пиво (более темное, насыщенное коричневое)
    barley_img = base_img.copy()
    data = barley_img.getdata()
    new_data = []
    for item in data:
        if item[3] > 0:
            r, g, b, a = item
            # Сдвигаем в сторону коричневого/темного
            r = int(r * 0.8)
            g = int(g * 0.7)
            b = int(b * 0.6)
            new_data.append((r, g, b, a))
        else:
            new_data.append(item)
    barley_img.putdata(new_data)
    barley_img.save("src/main/resources/assets/beer/textures/block/barley_beer.png")
    print("Barley beer texture created.")

create_malt_vat_gui()
create_beer_textures()