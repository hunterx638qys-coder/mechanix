#!/usr/bin/env python3
"""
Генератор ресурсов мода Mechanix: текстуры, модели, рецепты, лут, генерация мира, локализация.

Запуск из корня проекта:   python3 tools/gen_resources.py
(GitHub Actions запускает его автоматически перед сборкой.)

* PNG-текстуры НЕ перезаписываются, если файл уже существует:
  положите свою текстуру в src/main/resources/assets/mechanix/textures/... и она останется.
* Все JSON-файлы (модели, рецепты, лут, lang) создаются заново при каждом запуске.
  Чтобы изменить их, правьте этот скрипт.
"""
import json
import math
import random
import shutil
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/mechanix'
DATA = RES / 'data/mechanix'
TEX = ASSETS / 'textures'

# ---------- очистка старых json (текстуры не трогаем) ----------
for d in [ASSETS / 'lang', ASSETS / 'models', ASSETS / 'blockstates', DATA, RES / 'data/minecraft', RES / 'data/forge']:
    if d.exists():
        shutil.rmtree(d)


def w(path, content):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        if isinstance(content, (dict, list)):
            json.dump(content, f, ensure_ascii=False, indent=2)
        else:
            f.write(content)


# ====================================================================
#  PNG
# ====================================================================
class Canvas:
    def __init__(self, width=16, height=16):
        self.w, self.h = width, height
        self.p = [[(0, 0, 0, 0)] * width for _ in range(height)]

    def set(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.p[y][x] = c if len(c) == 4 else (*c, 255)

    def clear(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.p[y][x] = (0, 0, 0, 0)

    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

    def noise_rect(self, x0, y0, x1, y1, base, amp, seed):
        r = random.Random(seed)
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                d = r.randint(-amp, amp)
                self.set(x, y, tuple(max(0, min(255, v + d)) for v in base))

    def line(self, x0, y0, x1, y1, c):
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx = 1 if x0 < x1 else -1
        sy = 1 if y0 < y1 else -1
        err = dx + dy
        while True:
            self.set(x0, y0, c)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def pixels(self, pts, c):
        for (x, y) in pts:
            self.set(x, y, c)

    def circle(self, cx, cy, r, c):
        for y in range(self.h):
            for x in range(self.w):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r:
                    self.set(x, y, c)

    def border(self, c):
        for i in range(self.w):
            self.set(i, 0, c)
            self.set(i, self.h - 1, c)
        for i in range(self.h):
            self.set(0, i, c)
            self.set(self.w - 1, i, c)

    def save(self, path):
        path = Path(path)
        if path.exists():
            return  # не затираем пользовательские текстуры
        path.parent.mkdir(parents=True, exist_ok=True)
        raw = b''.join(b'\x00' + bytes(v for px in row for v in px) for row in self.p)

        def chunk(t, d):
            c = struct.pack('>I', len(d)) + t + d
            return c + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)

        png = (b'\x89PNG\r\n\x1a\n'
               + chunk(b'IHDR', struct.pack('>IIBBBBB', self.w, self.h, 8, 6, 0, 0, 0))
               + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b''))
        with open(path, 'wb') as f:
            f.write(png)


def pal(l, m, d):
    return {'l': l, 'm': m, 'd': d}


MITHRIL = pal((205, 235, 255), (120, 185, 235), (60, 110, 170))
STAR = pal((165, 165, 195), (90, 90, 125), (42, 42, 64))
BLOOD = pal((255, 135, 135), (190, 30, 45), (100, 10, 25))
IRONP = pal((225, 225, 230), (170, 170, 178), (110, 110, 120))
WOOD = (125, 85, 45)
WOOD_D = (80, 52, 25)
GOLD = (235, 200, 70)
GOLD_D = (170, 130, 30)


# ---------------- рисовалки предметов ----------------
def stick(c, x0=2, y0=14, x1=11, y1=5):
    c.line(x0, y0, x1, y1, WOOD)
    c.line(x0 + 1, y0, x1 + 1, y1, WOOD_D)


def sword(c, p, long=True):
    t = 13 if long else 11
    c.line(4, t - 2 + (0 if long else 0) + 0, 12 if long else 10, 3 if long else 5, p['l'])
    c.line(5, 11 if long else 10, 13 if long else 11, 3 if long else 5, p['m'])
    c.line(6, 11 if long else 10, 14 if long else 12, 3 if long else 5, p['d'])
    c.line(3, 9, 7, 13, GOLD)
    c.line(2, 14, 4, 12, WOOD)
    c.set(1, 15, GOLD_D)


def pickaxe(c, p):
    stick(c)
    c.pixels([(4, 5), (5, 3), (6, 2), (7, 2), (8, 2), (9, 2), (10, 3), (11, 4), (12, 5), (13, 6), (13, 7),
              (4, 6), (5, 4), (12, 6), (12, 7)], p['m'])
    c.pixels([(6, 2), (7, 2), (8, 2), (5, 3)], p['l'])
    c.pixels([(13, 7), (12, 7), (13, 6)], p['d'])


def axe(c, p):
    stick(c)
    c.pixels([(8, 2), (9, 2), (10, 2), (7, 3), (8, 3), (9, 3), (10, 3), (11, 3), (7, 4), (8, 4), (9, 4), (10, 4),
              (11, 4), (12, 4), (8, 5), (9, 5), (10, 5), (11, 5)], p['m'])
    c.pixels([(8, 2), (9, 2), (7, 3), (8, 3)], p['l'])
    c.pixels([(12, 4), (11, 5), (10, 5)], p['d'])


def shovel(c, p):
    stick(c)
    c.rect(10, 1, 13, 4, p['m'])
    c.rect(10, 1, 13, 1, p['l'])
    c.rect(13, 2, 13, 4, p['d'])
    c.clear(13, 1)


def hoe(c, p):
    stick(c)
    c.rect(8, 2, 13, 3, p['m'])
    c.rect(8, 2, 13, 2, p['l'])
    c.rect(8, 4, 9, 5, p['m'])
    c.rect(12, 3, 13, 3, p['d'])


def helmet(c, p):
    c.rect(4, 4, 11, 8, p['m'])
    c.rect(4, 4, 11, 5, p['l'])
    c.rect(4, 9, 5, 11, p['m'])
    c.rect(10, 9, 11, 11, p['m'])
    c.rect(4, 8, 11, 8, p['d'])
    c.rect(6, 7, 9, 8, (30, 30, 40))


def chestplate(c, p):
    c.rect(3, 3, 12, 13, p['m'])
    c.rect(1, 3, 3, 8, p['m'])
    c.rect(12, 3, 14, 8, p['m'])
    c.rect(6, 3, 9, 4, (0, 0, 0, 0))
    c.rect(3, 3, 12, 4, p['l'])
    c.rect(3, 12, 12, 13, p['d'])
    c.rect(7, 6, 8, 11, p['l'])


def leggings(c, p):
    c.rect(3, 2, 12, 5, p['m'])
    c.rect(3, 6, 6, 14, p['m'])
    c.rect(9, 6, 12, 14, p['m'])
    c.rect(3, 2, 12, 2, p['l'])
    c.rect(3, 13, 6, 14, p['d'])
    c.rect(9, 13, 12, 14, p['d'])


def boots(c, p):
    c.rect(2, 7, 6, 12, p['m'])
    c.rect(9, 7, 13, 12, p['m'])
    c.rect(1, 12, 6, 14, p['d'])
    c.rect(9, 12, 14, 14, p['d'])
    c.rect(2, 7, 6, 8, p['l'])
    c.rect(9, 7, 13, 8, p['l'])


def ingot(c, p):
    c.rect(3, 7, 12, 10, p['m'])
    c.rect(4, 6, 13, 6, p['l'])
    c.rect(13, 7, 13, 9, p['d'])
    c.rect(3, 10, 12, 10, p['d'])
    c.pixels([(5, 8), (6, 8)], p['l'])


def raw(c, p, seed=1):
    r = random.Random(seed)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 8)
            if d < 4.6 + r.uniform(-0.8, 0.8):
                c.set(x, y, p['m'] if r.random() > 0.3 else p['d'])
    c.pixels([(6, 6), (7, 6), (6, 7)], p['l'])


def gem(c, p):
    rows = {3: (7, 8), 4: (6, 9), 5: (5, 10), 6: (4, 11), 7: (4, 11), 8: (5, 10), 9: (5, 10), 10: (6, 9), 11: (6, 9),
            12: (7, 8), 13: (7, 8)}
    for y, (a, b) in rows.items():
        c.rect(a, y, b, y, p['m'])
    c.rect(7, 3, 8, 3, p['l'])
    c.pixels([(6, 4), (5, 5), (5, 6), (6, 5)], p['l'])
    c.pixels([(10, 7), (10, 8), (9, 10), (9, 11), (8, 12)], p['d'])


def crystal(c, p):
    c.rect(7, 2, 8, 12, p['m'])
    c.rect(6, 5, 9, 11, p['m'])
    c.rect(5, 7, 10, 10, p['m'])
    c.rect(7, 1, 8, 1, p['l'])
    c.rect(7, 2, 7, 11, p['l'])
    c.rect(9, 6, 10, 10, p['d'])
    c.pixels([(3, 10), (3, 11), (12, 9), (12, 10), (4, 11)], p['d'])


def crown(c):
    c.rect(2, 8, 13, 12, GOLD)
    c.pixels([(2, 5), (2, 6), (2, 7), (6, 5), (6, 6), (6, 7), (9, 5), (9, 6), (9, 7), (13, 5), (13, 6), (13, 7),
              (4, 6), (4, 7), (11, 6), (11, 7)], GOLD)
    c.rect(2, 12, 13, 12, GOLD_D)
    c.rect(7, 9, 8, 10, (60, 200, 90))
    c.pixels([(4, 10), (11, 10)], (200, 40, 50))


def banner(c):
    c.rect(3, 1, 3, 15, WOOD)
    c.rect(4, 2, 13, 11, (160, 25, 30))
    c.rect(4, 2, 13, 2, (210, 60, 55))
    c.pixels([(5, 12), (6, 12), (7, 12), (8, 11), (9, 12), (10, 12), (11, 12), (12, 12), (13, 12)], (160, 25, 30))
    c.rect(7, 5, 10, 8, (235, 200, 70))
    c.rect(8, 6, 9, 7, (160, 25, 30))


def core(c):
    c.circle(7.5, 8, 5, (110, 50, 170))
    c.circle(7.5, 8, 3.2, (170, 100, 230))
    c.circle(7.5, 8, 1.6, (130, 245, 255))
    c.pixels([(5, 5), (6, 4)], (220, 190, 255))


def blade(c, p, wide=False):
    sword(c, p)
    if wide:
        c.line(5, 10, 13, 2, p['l'])
        c.line(7, 11, 15, 3, p['d'])


def dagger(c, p):
    c.line(6, 9, 11, 4, p['l'])
    c.line(7, 9, 12, 4, p['m'])
    c.line(5, 7, 8, 10, GOLD)
    c.line(3, 12, 5, 10, WOOD)
    c.set(2, 13, GOLD_D)
    c.set(12, 3, p['l'])


def greataxe(c, p):
    c.line(2, 15, 11, 3, WOOD)
    c.line(3, 15, 12, 3, WOOD_D)
    for (x, y, wd) in [(7, 1, 7), (6, 2, 8), (6, 3, 9), (7, 4, 8), (8, 5, 6), (9, 6, 4)]:
        c.rect(x, y, min(15, x + wd - 1), y, p['m'])
    c.rect(7, 1, 14, 1, p['l'])
    c.rect(14, 2, 15, 4, p['d'])
    c.rect(8, 5, 9, 5, p['d'])


def bat(c):
    c.line(3, 14, 12, 3, (150, 105, 55))
    c.line(4, 14, 13, 3, (110, 75, 35))
    c.line(5, 13, 12, 6, (150, 105, 55))
    c.line(5, 14, 12, 7, (110, 75, 35))
    c.rect(11, 2, 13, 4, (170, 120, 65))
    c.pixels([(2, 15), (3, 15)], (60, 40, 20))


def staff(c, orb_l, orb_m, orb_d):
    c.line(2, 15, 10, 7, WOOD)
    c.line(3, 15, 11, 7, WOOD_D)
    c.circle(11.5, 4.5, 3, orb_m)
    c.circle(11.5, 4.5, 3, orb_m)
    c.pixels([(10, 3), (11, 2), (10, 2)], orb_l)
    c.pixels([(13, 6), (12, 7), (13, 5)], orb_d)
    c.pixels([(9, 6), (9, 7), (10, 8)], GOLD)


def potion(c, liquid, dark, big=False):
    c.rect(6, 1, 9, 2, (150, 110, 60))
    c.rect(7, 3, 8, 5, (200, 220, 230))
    top = 5 if big else 6
    c.rect(4 if big else 5, top, 11 if big else 10, 13, (200, 220, 230))
    c.rect(5 if big else 6, top + 1, 10 if big else 9, 12, liquid)
    c.rect(5 if big else 6, 11, 10 if big else 9, 12, dark)
    c.pixels([(6, top + 1), (6, top + 2)], (255, 255, 255))


def tome(c, cover, trim):
    c.rect(3, 2, 12, 13, cover)
    c.rect(3, 2, 4, 13, trim)
    c.rect(5, 3, 12, 3, trim)
    c.rect(5, 12, 12, 12, trim)
    c.rect(11, 3, 11, 12, (235, 230, 210))
    c.circle(8, 7.5, 2, GOLD)
    c.pixels([(8, 5), (8, 10), (6, 7), (10, 7)], GOLD_D)


def scroll(c, seal):
    c.rect(3, 3, 12, 12, (235, 225, 190))
    c.rect(2, 2, 13, 3, (190, 160, 100))
    c.rect(2, 12, 13, 13, (190, 160, 100))
    c.line(5, 6, 10, 6, (120, 90, 60))
    c.line(5, 8, 10, 8, (120, 90, 60))
    c.circle(10, 10, 2, seal)


def moonroot(c):
    c.pixels([(7, 1), (8, 1), (6, 2), (7, 2), (9, 2), (10, 2)], (80, 190, 110))
    for (x, y, wd) in [(5, 4, 6), (4, 5, 8), (4, 6, 8), (5, 7, 6), (5, 8, 6), (6, 9, 4), (6, 10, 4), (7, 11, 2), (7, 12, 2)]:
        c.rect(x, y, x + wd - 1, y, (140, 90, 200))
    c.rect(5, 4, 9, 4, (190, 150, 240))
    c.rect(11, 5, 11, 7, (90, 55, 140))
    c.pixels([(7, 13), (8, 14)], (140, 90, 200))


def seeds(c):
    for (x, y) in [(4, 5), (8, 4), (11, 6), (6, 8), (10, 10), (5, 11), (8, 12)]:
        c.rect(x, y, x + 1, y + 1, (60, 40, 110))
        c.set(x, y, (140, 100, 210))


def stew(c):
    c.rect(2, 8, 13, 9, (130, 90, 55))
    c.rect(3, 10, 12, 12, (110, 75, 45))
    c.rect(5, 13, 10, 13, (90, 60, 35))
    c.rect(3, 7, 12, 8, (150, 90, 40))
    c.pixels([(5, 7), (8, 7), (10, 7)], (190, 130, 60))
    c.pixels([(6, 6), (9, 6)], (120, 190, 110))
    c.pixels([(7, 3), (8, 2), (7, 1), (9, 4)], (230, 230, 230))


def guide(c):
    tome(c, (40, 70, 150), (30, 45, 100))
    c.circle(8, 7.5, 2, (130, 245, 255))


def wand(c):
    c.line(3, 14, 11, 6, (60, 60, 70))
    c.line(4, 14, 12, 6, (30, 30, 40))
    cols = [(255, 80, 80), (255, 200, 60), (90, 220, 110), (80, 180, 255), (190, 110, 255)]
    for i, col in enumerate(cols):
        c.set(10 + (i % 3), 4 - i // 2 + 1, col)
    c.rect(11, 3, 13, 5, (255, 255, 255))
    c.set(12, 4, (255, 220, 120))


# ---------------- регистрация предметов ----------------
ITEM_DEFS = []  # (id, ru, en, drawer, model)  model: 'generated' | 'handheld' | 'egg'


def item(iid, ru, en, drawer=None, model='generated'):
    ITEM_DEFS.append((iid, ru, en, drawer, model))


item('raw_mithril', 'Сырой мифрил', 'Raw Mithril', lambda c: raw(c, MITHRIL, 11))
item('mithril_ingot', 'Мифриловый слиток', 'Mithril Ingot', lambda c: ingot(c, MITHRIL))
item('raw_star_iron', 'Сырое звёздное железо', 'Raw Star Iron', lambda c: raw(c, STAR, 12))
item('star_iron_ingot', 'Слиток звёздного железа', 'Star Iron Ingot', lambda c: ingot(c, STAR))
item('bloodstone', 'Кровавый камень', 'Bloodstone', lambda c: gem(c, BLOOD))
item('mana_crystal', 'Кристалл маны', 'Mana Crystal', lambda c: crystal(c, pal((190, 230, 255), (70, 140, 255), (30, 70, 190))))
item('goblin_crown', 'Корона короля гоблинов', 'Goblin King Crown', crown)
item('orc_war_banner', 'Боевое знамя орков', 'Orc War Banner', banner)
item('lich_core', 'Ядро лича', 'Lich Core', core)

for mat, palette, ru_m, en_m in [('mithril', MITHRIL, 'Мифриловый', 'Mithril'), ('star_iron', STAR, 'Из звёздного железа', 'Star Iron')]:
    names = {
        'mithril': {'sword': ('Мифриловый меч', 'Mithril Sword'), 'pickaxe': ('Мифриловая кирка', 'Mithril Pickaxe'),
                    'axe': ('Мифриловый топор', 'Mithril Axe'), 'shovel': ('Мифриловая лопата', 'Mithril Shovel'),
                    'hoe': ('Мифриловая мотыга', 'Mithril Hoe')},
        'star_iron': {'sword': ('Меч из звёздного железа', 'Star Iron Sword'), 'pickaxe': ('Кирка из звёздного железа', 'Star Iron Pickaxe'),
                      'axe': ('Топор из звёздного железа', 'Star Iron Axe'), 'shovel': ('Лопата из звёздного железа', 'Star Iron Shovel'),
                      'hoe': ('Мотыга из звёздного железа', 'Star Iron Hoe')},
    }[mat]
    for kind, fn in [('sword', sword), ('pickaxe', pickaxe), ('axe', axe), ('shovel', shovel), ('hoe', hoe)]:
        item(f'{mat}_{kind}', names[kind][0], names[kind][1], (lambda f, p: (lambda c: f(c, p)))(fn, palette), 'handheld')

ARMOR_NAMES = {
    'mithril': ('Мифриловый', 'Мифриловый', 'Мифриловые', 'Мифриловые', 'Mithril'),
    'star_iron': ('Шлем', 'Нагрудник', 'Поножи', 'Ботинки', 'Star Iron'),
}
ARMOR_RU = {
    'mithril': {'helmet': 'Мифриловый шлем', 'chestplate': 'Мифриловый нагрудник', 'leggings': 'Мифриловые поножи', 'boots': 'Мифриловые ботинки'},
    'star_iron': {'helmet': 'Шлем из звёздного железа', 'chestplate': 'Нагрудник из звёздного железа', 'leggings': 'Поножи из звёздного железа', 'boots': 'Ботинки из звёздного железа'},
    'bloodstone': {'helmet': 'Шлем из кровавого камня', 'chestplate': 'Нагрудник из кровавого камня', 'leggings': 'Поножи из кровавого камня', 'boots': 'Ботинки из кровавого камня'},
}
ARMOR_EN = {'mithril': 'Mithril', 'star_iron': 'Star Iron', 'bloodstone': 'Bloodstone'}
PIECE_EN = {'helmet': 'Helmet', 'chestplate': 'Chestplate', 'leggings': 'Leggings', 'boots': 'Boots'}
for mat, palette in [('mithril', MITHRIL), ('star_iron', STAR), ('bloodstone', BLOOD)]:
    for piece, fn in [('helmet', helmet), ('chestplate', chestplate), ('leggings', leggings), ('boots', boots)]:
        item(f'{mat}_{piece}', ARMOR_RU[mat][piece], f'{ARMOR_EN[mat]} {PIECE_EN[piece]}',
             (lambda f, p: (lambda c: f(c, p)))(fn, palette))

item('bloodstone_blade', 'Сердцерез', 'Heartreaver', lambda c: blade(c, BLOOD), 'handheld')
item('abyss_blade', 'Клинок Бездны', 'Abyss Blade', lambda c: blade(c, pal((200, 150, 255), (110, 50, 190), (50, 15, 100)), True), 'handheld')
item('goblin_dagger', 'Кинжал гоблина', 'Goblin Dagger', lambda c: dagger(c, pal((190, 230, 160), (120, 170, 90), (70, 110, 55))), 'handheld')
item('orc_greataxe', 'Секира варлорда', 'Warlord Greataxe', lambda c: greataxe(c, STAR), 'handheld')
item('bonk_bat', 'Бита Бонк', 'Bonk Bat', bat, 'handheld')
item('fire_staff', 'Огненный посох', 'Fire Staff', lambda c: staff(c, (255, 230, 120), (255, 110, 30), (170, 40, 10)), 'handheld')
item('frost_staff', 'Ледяной посох', 'Frost Staff', lambda c: staff(c, (230, 255, 255), (110, 210, 255), (50, 120, 200)), 'handheld')
item('lightning_staff', 'Посох молний', 'Lightning Staff', lambda c: staff(c, (255, 255, 200), (255, 230, 60), (190, 150, 10)), 'handheld')
item('healing_staff', 'Посох исцеления', 'Healing Staff', lambda c: staff(c, (220, 255, 220), (90, 220, 120), (30, 140, 70)), 'handheld')
item('mana_potion', 'Зелье маны', 'Mana Potion', lambda c: potion(c, (60, 130, 255), (30, 70, 190)))
item('great_mana_potion', 'Большое зелье маны', 'Great Mana Potion', lambda c: potion(c, (150, 80, 255), (90, 40, 190), True))
item('experience_tome', 'Том опыта', 'Tome of Experience', lambda c: tome(c, (120, 40, 150), (80, 20, 100)))
item('rebirth_scroll', 'Свиток перерождения', 'Rebirth Scroll', lambda c: scroll(c, (200, 40, 50)))
item('reset_scroll', 'Свиток сброса', 'Reset Scroll', lambda c: scroll(c, (50, 110, 230)))
item('moonroot', 'Лунный корень', 'Moonroot', moonroot)
item('moonroot_seeds', 'Семена лунного корня', 'Moonroot Seeds', seeds)
item('hunter_stew', 'Рагу охотника', "Hunter's Stew", stew)
item('rpg_guide', 'Путеводитель Mechanix', 'Mechanix Guide', guide)
item('creative_wand', 'Жезл творца', "Creator's Wand", wand, 'handheld')
for egg, ru, en in [('goblin', 'Яйцо призыва: Гоблин', 'Goblin Spawn Egg'), ('orc', 'Яйцо призыва: Орк', 'Orc Spawn Egg'),
                    ('skeleton_knight', 'Яйцо призыва: Скелет-рыцарь', 'Skeleton Knight Spawn Egg'),
                    ('cave_stalker', 'Яйцо призыва: Пещерный охотник', 'Cave Stalker Spawn Egg'),
                    ('goblin_king', 'Яйцо призыва: Король гоблинов', 'Goblin King Spawn Egg'),
                    ('orc_warlord', 'Яйцо призыва: Варлорд орков', 'Orc Warlord Spawn Egg'),
                    ('lich', 'Яйцо призыва: Лич', 'Lich Spawn Egg')]:
    item(f'{egg}_spawn_egg', ru, en, None, 'egg')

BLOCK_ITEMS = [('mithril_ore', 'Мифриловая руда', 'Mithril Ore'), ('star_iron_ore', 'Руда звёздного железа', 'Star Iron Ore'),
               ('bloodstone_ore', 'Руда кровавого камня', 'Bloodstone Ore'), ('mana_crystal_ore', 'Руда кристаллов маны', 'Mana Crystal Ore')]

# ---------------- текстуры предметов + модели ----------------
for iid, ru, en, drawer, model in ITEM_DEFS:
    if drawer is not None:
        cv = Canvas()
        drawer(cv)
        cv.save(TEX / f'item/{iid}.png')
    if model == 'egg':
        w(ASSETS / f'models/item/{iid}.json', {'parent': 'minecraft:item/template_spawn_egg'})
    else:
        parent = 'minecraft:item/handheld' if model == 'handheld' else 'minecraft:item/generated'
        w(ASSETS / f'models/item/{iid}.json', {'parent': parent, 'textures': {'layer0': f'mechanix:item/{iid}'}})

# ---------------- блоки ----------------
def ore_texture(name, lc, mc, dc, seed):
    c = Canvas()
    c.noise_rect(0, 0, 15, 15, (125, 125, 128), 12, seed)
    r = random.Random(seed * 7)
    for _ in range(6):
        cx, cy = r.randint(2, 13), r.randint(2, 13)
        for (dx, dy) in [(0, 0), (1, 0), (0, 1), (1, 1), (-1, 0)]:
            if r.random() > 0.15:
                c.set(cx + dx, cy + dy, mc)
        c.set(cx, cy, lc)
        c.set(cx + 1, cy + 1, dc)
    c.save(TEX / f'block/{name}.png')


ore_texture('mithril_ore', MITHRIL['l'], MITHRIL['m'], MITHRIL['d'], 21)
ore_texture('star_iron_ore', GOLD, STAR['m'], STAR['d'], 22)
ore_texture('bloodstone_ore', BLOOD['l'], BLOOD['m'], BLOOD['d'], 23)
ore_texture('mana_crystal_ore', (190, 230, 255), (70, 140, 255), (30, 70, 190), 24)

for name, _, _ in BLOCK_ITEMS:
    w(ASSETS / f'blockstates/{name}.json', {'variants': {'': {'model': f'mechanix:block/{name}'}}})
    w(ASSETS / f'models/block/{name}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'mechanix:block/{name}'}})
    w(ASSETS / f'models/item/{name}.json', {'parent': f'mechanix:block/{name}'})

# культура: 4 стадии роста
for stage in range(4):
    c = Canvas()
    h = [3, 6, 9, 12][stage]
    for x in (3, 7, 11):
        c.rect(x, 15 - h, x + 1, 15, (60, 150, 90))
        c.rect(x, 15 - h, x + 1, 15 - h + 1, (110, 210, 140))
    if stage == 3:
        for x in (3, 7, 11):
            c.rect(x, 12, x + 1, 14, (150, 100, 220))
            c.set(x, 12, (210, 170, 255))
    c.save(TEX / f'block/moonroot_stage{stage}.png')
    w(ASSETS / f'models/block/moonroot_stage{stage}.json', {'parent': 'minecraft:block/crop', 'textures': {'crop': f'mechanix:block/moonroot_stage{stage}'}})
stage_for_age = [0, 0, 1, 1, 2, 2, 2, 3]
w(ASSETS / 'blockstates/moonroot_crop.json', {'variants': {f'age={a}': {'model': f'mechanix:block/moonroot_stage{stage_for_age[a]}'} for a in range(8)}})

# ---------------- текстуры мобов ----------------
def humanoid64(name, skin, cloth, pants, eyes, seed, trim=None, crown_c=None):
    c = Canvas(64, 64)
    c.noise_rect(0, 0, 31, 15, skin, 10, seed)
    c.noise_rect(16, 16, 39, 31, cloth, 10, seed + 1)
    c.noise_rect(40, 16, 55, 31, skin, 10, seed + 2)
    c.noise_rect(0, 16, 15, 31, pants, 10, seed + 3)
    c.noise_rect(16, 48, 31, 63, pants, 10, seed + 4)
    c.noise_rect(32, 48, 47, 63, skin, 10, seed + 5)
    c.rect(40, 20, 55, 23, cloth)
    c.rect(32, 52, 47, 55, cloth)
    if trim:
        c.rect(20, 28, 35, 29, trim)
        c.rect(20, 20, 35, 20, trim)
    for x in (9, 10, 13, 14):
        c.set(x, 12, eyes)
    c.rect(9, 11, 10, 11, tuple(max(0, v - 50) for v in skin))
    c.rect(13, 11, 14, 11, tuple(max(0, v - 50) for v in skin))
    c.rect(10, 14, 13, 14, tuple(max(0, v - 60) for v in skin))
    c.set(11, 14, (230, 230, 220))
    c.set(12, 14, (230, 230, 220))
    if crown_c:
        c.rect(8, 0, 15, 7, crown_c)
        for x in range(8, 16, 2):
            c.set(x, 8, crown_c)
            c.set(x, 9, crown_c)
        c.rect(0, 8, 31, 8, crown_c)
        c.set(11, 9, (60, 220, 100))
        c.set(12, 9, (60, 220, 100))
    c.save(TEX / f'entity/{name}.png')


def skeleton32(name, bone, dark, glow, seed, robe=None):
    c = Canvas(64, 32)
    c.noise_rect(0, 0, 31, 15, bone, 8, seed)
    c.noise_rect(16, 16, 39, 31, bone, 8, seed + 1)
    c.noise_rect(40, 16, 55, 31, bone, 8, seed + 2)
    c.noise_rect(0, 16, 15, 31, bone, 8, seed + 3)
    for y in range(18, 31, 3):
        c.rect(16, y, 39, y, dark)
    for x in (9, 10, 13, 14):
        c.rect(x, 11, x, 12, (20, 20, 25))
    c.set(10, 12, glow)
    c.set(13, 12, glow)
    c.rect(10, 14, 13, 14, dark)
    if robe:
        c.noise_rect(16, 16, 39, 31, robe, 8, seed + 9)
        c.noise_rect(0, 16, 15, 27, robe, 8, seed + 10)
        c.rect(40, 16, 55, 22, robe)
        c.rect(20, 20, 35, 20, glow)
    c.save(TEX / f'entity/{name}.png')


def spider32(name, base, eyes, seed):
    c = Canvas(64, 32)
    c.noise_rect(0, 0, 63, 31, base, 12, seed)
    for (x, y) in [(41, 15), (42, 15), (45, 15), (46, 15), (41, 17), (46, 17), (43, 14), (44, 14)]:
        c.set(x, y, eyes)
    c.save(TEX / f'entity/{name}.png')


humanoid64('goblin', (105, 150, 60), (110, 75, 40), (75, 55, 35), (255, 230, 60), 31, trim=(70, 45, 25))
humanoid64('orc', (70, 120, 55), (85, 85, 95), (60, 50, 45), (255, 60, 40), 41, trim=(150, 30, 30))
humanoid64('goblin_king', (95, 140, 55), (150, 30, 40), (100, 25, 30), (255, 60, 40), 51, trim=GOLD, crown_c=GOLD)
humanoid64('orc_warlord', (50, 85, 45), (35, 35, 45), (30, 30, 35), (255, 140, 30), 61, trim=(190, 25, 25))
skeleton32('skeleton_knight', (200, 200, 205), (90, 90, 100), (120, 200, 255), 71, robe=None)
skeleton32('lich', (190, 185, 210), (70, 50, 110), (130, 245, 255), 81, robe=(60, 25, 90))
spider32('cave_stalker', (38, 26, 58), (255, 40, 40), 91)

# ---------------- текстуры брони (на модель) ----------------
def armor_layer(mat, layer, palette):
    c = Canvas(64, 32)
    c.noise_rect(0, 0, 63, 31, palette['m'], 10, zlib.crc32(mat.encode()) % 1000 + layer)
    for y in range(0, 32, 4):
        c.rect(0, y, 63, y, palette['d'])
    for x in range(0, 64, 8):
        c.rect(x, 0, x, 31, palette['l'])
    c.save(TEX / f'models/armor/{mat}_layer_{layer}.png')


for mat, palette in [('mithril', MITHRIL), ('star_iron', STAR), ('bloodstone', BLOOD)]:
    armor_layer(mat, 1, palette)
    armor_layer(mat, 2, palette)

# ---------------- тайлы обновления иконки мода ----------------


# ====================================================================
#  ЧАСТЬ 2: рецепты, лут, генерация мира, теги, локализация
# ====================================================================
M = 'mechanix:'


def it(name):
    return {'item': name if ':' in name else M + name}


def tg(name):
    return {'tag': name}


def recipe(rid, data):
    w(DATA / f'recipes/{rid}.json', data)


def shaped(rid, pattern, key, result, count=1):
    recipe(rid, {'type': 'minecraft:crafting_shaped', 'category': 'equipment', 'pattern': pattern, 'key': key,
                 'result': {'item': M + result, 'count': count}})


def shapeless(rid, ingredients, result, count=1):
    recipe(rid, {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': ingredients,
                 'result': {'item': M + result, 'count': count}})


# ---------- инструменты и броня ----------
TOOL_PATTERNS = {
    'sword': ['I', 'I', 'S'],
    'pickaxe': ['III', ' S ', ' S '],
    'axe': ['II', 'IS', ' S'],
    'shovel': ['I', 'S', 'S'],
    'hoe': ['II', ' S', ' S'],
}
ARMOR_PATTERNS = {
    'helmet': ['III', 'I I'],
    'chestplate': ['I I', 'III', 'III'],
    'leggings': ['III', 'I I', 'I I'],
    'boots': ['I I', 'I I'],
}
for mat, base in [('mithril', 'mithril_ingot'), ('star_iron', 'star_iron_ingot')]:
    for kind, pat in TOOL_PATTERNS.items():
        shaped(f'{mat}_{kind}', pat, {'I': it(base), 'S': it('minecraft:stick')}, f'{mat}_{kind}')
for mat, base in [('mithril', 'mithril_ingot'), ('star_iron', 'star_iron_ingot'), ('bloodstone', 'bloodstone')]:
    for kind, pat in ARMOR_PATTERNS.items():
        shaped(f'{mat}_{kind}', pat, {'I': it(base)}, f'{mat}_{kind}')

shaped('bloodstone_blade', ['B', 'B', 'S'], {'B': it('bloodstone'), 'S': it('minecraft:stick')}, 'bloodstone_blade')
shaped('bonk_bat', [' P ', ' P ', ' S '], {'P': tg('minecraft:planks'), 'S': it('minecraft:stick')}, 'bonk_bat')

# ---------- плавка ----------
for src, dst, xp in [('raw_mithril', 'mithril_ingot', 0.7), ('mithril_ore', 'mithril_ingot', 0.7),
                     ('raw_star_iron', 'star_iron_ingot', 1.0), ('star_iron_ore', 'star_iron_ingot', 1.0),
                     ('bloodstone_ore', 'bloodstone', 0.8), ('mana_crystal_ore', 'mana_crystal', 0.6)]:
    for kind, t in [('smelting', 200), ('blasting', 100)]:
        recipe(f'{dst}_from_{kind}_{src}', {'type': f'minecraft:{kind}', 'category': 'misc', 'ingredient': it(src),
                                              'result': M + dst, 'experience': xp, 'cookingtime': t})

# ---------- посохи ----------
for staff_id, extra in [('fire_staff', 'minecraft:blaze_powder'), ('frost_staff', 'minecraft:packed_ice'),
                        ('lightning_staff', 'minecraft:copper_ingot'), ('healing_staff', 'minecraft:glistering_melon_slice')]:
    shaped(staff_id, [' M ', 'XSX', ' S '],
           {'M': it('mana_crystal'), 'X': it(extra), 'S': it('minecraft:stick')}, staff_id)

# ---------- зелья, свитки, прочее ----------
shapeless('mana_potion', [it('minecraft:glass_bottle'), it('mana_crystal'), it('minecraft:lapis_lazuli')], 'mana_potion')
shapeless('great_mana_potion', [it('mana_potion'), it('mana_crystal'), it('mana_crystal'), it('minecraft:glowstone_dust')],
          'great_mana_potion')
shapeless('experience_tome', [it('minecraft:book'), it('minecraft:emerald'), it('mana_crystal'), it('mana_crystal')],
          'experience_tome')
shapeless('rebirth_scroll', [it('minecraft:paper'), it('bloodstone'), it('bloodstone'), it('minecraft:ender_pearl')],
          'rebirth_scroll')
shapeless('reset_scroll', [it('minecraft:paper'), it('mana_crystal'), it('minecraft:lapis_lazuli'), it('minecraft:redstone')],
          'reset_scroll')
shapeless('rpg_guide', [it('minecraft:book'), it('minecraft:feather')], 'rpg_guide')
shapeless('moonroot_seeds', [it('minecraft:wheat_seeds'), it('mana_crystal')], 'moonroot_seeds', 2)
shapeless('hunter_stew', [it('minecraft:bowl'), it('moonroot'), it('moonroot'), it('minecraft:cooked_beef')], 'hunter_stew')


# ---------- лут: блоки ----------
def silk_cond():
    return [{'condition': 'minecraft:match_tool',
             'predicate': {'enchantments': [{'enchantment': 'minecraft:silk_touch', 'levels': {'min': 1}}]}}]


def ore_loot(block, drop, count=None):
    funcs = []
    if count:
        funcs.append({'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': count[0], 'max': count[1]}})
    funcs.append({'function': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune', 'formula': 'minecraft:ore_drops'})
    funcs.append({'function': 'minecraft:explosion_decay'})
    w(DATA / f'loot_tables/blocks/{block}.json', {
        'type': 'minecraft:block',
        'pools': [{'rolls': 1.0, 'bonus_rolls': 0.0, 'entries': [{
            'type': 'minecraft:alternatives',
            'children': [
                {'type': 'minecraft:item', 'name': M + block, 'conditions': silk_cond()},
                {'type': 'minecraft:item', 'name': M + drop, 'functions': funcs},
            ]}]}]})


ore_loot('mithril_ore', 'raw_mithril')
ore_loot('star_iron_ore', 'raw_star_iron')
ore_loot('bloodstone_ore', 'bloodstone')
ore_loot('mana_crystal_ore', 'mana_crystal', (1, 3))

ripe = [{'condition': 'minecraft:block_state_property', 'block': M + 'moonroot_crop', 'properties': {'age': '7'}}]
w(DATA / 'loot_tables/blocks/moonroot_crop.json', {
    'type': 'minecraft:block',
    'pools': [
        {'rolls': 1.0, 'bonus_rolls': 0.0, 'entries': [{
            'type': 'minecraft:alternatives',
            'children': [
                {'type': 'minecraft:item', 'name': M + 'moonroot', 'conditions': ripe},
                {'type': 'minecraft:item', 'name': M + 'moonroot_seeds'},
            ]}]},
        {'rolls': 1.0, 'bonus_rolls': 0.0, 'conditions': ripe, 'entries': [{
            'type': 'minecraft:item', 'name': M + 'moonroot_seeds',
            'functions': [{'function': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune',
                           'formula': 'minecraft:binomial_with_bonus_count',
                           'parameters': {'extra': 3, 'probability': 0.5714286}}]}]},
    ]})


# ---------- лут: мобы ----------
def drop(name, lo=1, hi=1, chance=None, looting=0):
    e = {'type': 'minecraft:item', 'name': name if ':' in name else M + name, 'functions': []}
    if (lo, hi) != (1, 1):
        e['functions'].append({'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': lo, 'max': hi}})
    if looting:
        e['functions'].append({'function': 'minecraft:looting_enchant', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': looting}})
    if not e['functions']:
        del e['functions']
    pool = {'rolls': 1.0, 'bonus_rolls': 0.0, 'entries': [e]}
    if chance is not None:
        pool['conditions'] = [{'condition': 'minecraft:random_chance_with_looting', 'chance': chance, 'looting_multiplier': 0.02}]
    return pool


def mob_loot(mob, pools):
    w(DATA / f'loot_tables/entities/{mob}.json', {'type': 'minecraft:entity', 'pools': pools})


mob_loot('goblin', [drop('minecraft:gold_nugget', 0, 3, looting=1), drop('minecraft:emerald', chance=0.06),
                    drop('goblin_dagger', chance=0.04), drop('moonroot_seeds', chance=0.08)])
mob_loot('orc', [drop('minecraft:bone', 0, 2, looting=1), drop('minecraft:iron_nugget', 1, 4, looting=2),
                 drop('raw_star_iron', chance=0.05), drop('mana_potion', chance=0.06)])
mob_loot('skeleton_knight', [drop('minecraft:bone', 1, 2, looting=1), drop('minecraft:iron_ingot', chance=0.15),
                             drop('mana_crystal', chance=0.10), drop('raw_mithril', chance=0.05)])
mob_loot('cave_stalker', [drop('minecraft:string', 1, 3, looting=1), drop('minecraft:spider_eye', chance=0.3),
                          drop('bloodstone', chance=0.08)])
mob_loot('goblin_king', [drop('goblin_crown'), drop('minecraft:gold_ingot', 4, 9), drop('mana_crystal', 2, 5),
                         drop('goblin_dagger', chance=0.5), drop('experience_tome', chance=0.35),
                         drop('great_mana_potion', 1, 2)])
mob_loot('orc_warlord', [drop('orc_war_banner'), drop('raw_star_iron', 3, 6), drop('mana_crystal', 2, 4),
                         drop('orc_greataxe', chance=0.35), drop('experience_tome', chance=0.4),
                         drop('great_mana_potion', 1, 2)])
mob_loot('lich', [drop('lich_core'), drop('mana_crystal', 4, 8), drop('abyss_blade', chance=0.3),
                  drop('experience_tome', 1, 2), drop('rebirth_scroll', chance=0.15), drop('great_mana_potion', 1, 3)])


# ---------- лут: сундуки структур ----------
def chest(name, rolls, entries):
    pool_entries = []
    for item_name, weight, lo, hi in entries:
        e = {'type': 'minecraft:item', 'name': item_name if ':' in item_name else M + item_name, 'weight': weight}
        if (lo, hi) != (1, 1):
            e['functions'] = [{'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': lo, 'max': hi}}]
        pool_entries.append(e)
    w(DATA / f'loot_tables/chests/{name}.json', {
        'type': 'minecraft:chest',
        'pools': [{'rolls': {'type': 'minecraft:uniform', 'min': rolls[0], 'max': rolls[1]}, 'bonus_rolls': 0.0,
                   'entries': pool_entries}]})


chest('mage_tower', (4, 7), [
    ('mana_crystal', 20, 2, 6), ('mana_potion', 16, 1, 3), ('great_mana_potion', 6, 1, 1),
    ('experience_tome', 6, 1, 1), ('reset_scroll', 4, 1, 1), ('rebirth_scroll', 2, 1, 1),
    ('fire_staff', 3, 1, 1), ('frost_staff', 3, 1, 1), ('lightning_staff', 2, 1, 1), ('healing_staff', 3, 1, 1),
    ('moonroot_seeds', 8, 2, 5), ('minecraft:book', 10, 1, 3), ('minecraft:lapis_lazuli', 10, 3, 8),
    ('rpg_guide', 4, 1, 1), ('mithril_ingot', 5, 1, 3)])
chest('goblin_camp', (3, 6), [
    ('minecraft:gold_nugget', 24, 4, 14), ('minecraft:gold_ingot', 10, 1, 3), ('minecraft:emerald', 8, 1, 3),
    ('goblin_dagger', 3, 1, 1), ('mana_potion', 8, 1, 2), ('moonroot_seeds', 8, 2, 4), ('moonroot', 8, 1, 3),
    ('hunter_stew', 5, 1, 1), ('minecraft:bread', 12, 2, 5), ('minecraft:iron_ingot', 10, 1, 4),
    ('raw_mithril', 4, 1, 3), ('bonk_bat', 1, 1, 1)])
chest('orc_ruins', (3, 6), [
    ('minecraft:iron_ingot', 18, 2, 6), ('minecraft:bone', 12, 2, 6), ('minecraft:cooked_beef', 12, 2, 5),
    ('raw_star_iron', 8, 1, 3), ('raw_mithril', 8, 1, 3), ('bloodstone', 6, 1, 2), ('mana_potion', 8, 1, 2),
    ('great_mana_potion', 3, 1, 1), ('experience_tome', 3, 1, 1), ('minecraft:golden_apple', 2, 1, 1),
    ('hunter_stew', 5, 1, 1), ('bloodstone_blade', 1, 1, 1)])

# ---------- генерация мира: руды ----------
ORES = {  # id: (size, count, min_y, max_y)
    'mithril_ore': (6, 5, -60, 24),
    'star_iron_ore': (4, 2, -60, -8),
    'bloodstone_ore': (5, 4, -48, 0),
    'mana_crystal_ore': (7, 6, -50, 40),
}
for ore, (size, count, y0, y1) in ORES.items():
    w(DATA / f'worldgen/configured_feature/{ore}.json', {
        'type': 'minecraft:ore',
        'config': {'size': size, 'discard_chance_on_air_exposure': 0.0, 'targets': [
            {'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:stone_ore_replaceables'},
             'state': {'Name': M + ore}},
            {'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:deepslate_ore_replaceables'},
             'state': {'Name': M + ore}}]}})
    w(DATA / f'worldgen/placed_feature/{ore}.json', {
        'feature': M + ore,
        'placement': [
            {'type': 'minecraft:count', 'count': count},
            {'type': 'minecraft:in_square'},
            {'type': 'minecraft:height_range', 'height': {
                'type': 'minecraft:uniform', 'min_inclusive': {'absolute': y0}, 'max_inclusive': {'absolute': y1}}},
            {'type': 'minecraft:biome'}]})
    w(DATA / f'forge/biome_modifier/{ore}.json', {
        'type': 'forge:add_features', 'biomes': '#minecraft:is_overworld',
        'features': M + ore, 'step': 'underground_ores'})

# ---------- генерация мира: структуры ----------
STRUCTS = {  # id: (редкость (1 из N чанков), теги биомов)
    'mage_tower': (110, ['#minecraft:is_forest', '#minecraft:is_taiga', '#minecraft:is_hill', 'minecraft:plains',
                         'minecraft:meadow', 'minecraft:desert', 'minecraft:savanna']),
    'goblin_camp': (70, ['#minecraft:is_forest', '#minecraft:is_taiga', 'minecraft:plains', 'minecraft:swamp',
                         'minecraft:savanna']),
    'orc_ruins': (90, ['#minecraft:is_hill', '#minecraft:is_mountain', '#minecraft:is_badlands', 'minecraft:desert',
                       'minecraft:savanna_plateau']),
}
for sid, (chance, biomes) in STRUCTS.items():
    w(DATA / f'worldgen/configured_feature/{sid}.json', {'type': M + sid, 'config': {}})
    w(DATA / f'worldgen/placed_feature/{sid}.json', {
        'feature': M + sid,
        'placement': [
            {'type': 'minecraft:rarity_filter', 'chance': chance},
            {'type': 'minecraft:in_square'},
            {'type': 'minecraft:heightmap', 'heightmap': 'WORLD_SURFACE_WG'},
            {'type': 'minecraft:biome'}]})
    w(DATA / f'tags/worldgen/biome/{sid}_biomes.json', {'values': biomes})
    w(DATA / f'forge/biome_modifier/{sid}.json', {
        'type': 'forge:add_features', 'biomes': f'#mechanix:{sid}_biomes',
        'features': M + sid, 'step': 'surface_structures'})

# ---------- спавн мобов ----------
SPAWNS = [  # моб, биомы, вес, мин, макс
    ('goblin', '#mechanix:goblin_camp_biomes', 10, 2, 4),
    ('orc', '#mechanix:orc_ruins_biomes', 6, 1, 3),
    ('skeleton_knight', '#minecraft:is_overworld', 4, 1, 2),
    ('cave_stalker', '#minecraft:is_overworld', 3, 1, 2),
]
for mob, biomes, weight, mn, mx in SPAWNS:
    w(DATA / f'forge/biome_modifier/spawn_{mob}.json', {
        'type': 'forge:add_spawns', 'biomes': biomes,
        'spawners': {'type': M + mob, 'weight': weight, 'minCount': mn, 'maxCount': mx}})

# ---------- теги ----------
ORE_IDS = ['mithril_ore', 'star_iron_ore', 'bloodstone_ore', 'mana_crystal_ore']


def tag(path, values):
    w(RES / path, {'replace': False, 'values': values})


tag('data/minecraft/tags/blocks/mineable/pickaxe.json', [M + o for o in ORE_IDS])
tag('data/minecraft/tags/blocks/needs_stone_tool.json', [M + 'mana_crystal_ore'])
tag('data/minecraft/tags/blocks/needs_iron_tool.json', [M + 'mithril_ore', M + 'bloodstone_ore'])
tag('data/minecraft/tags/blocks/needs_diamond_tool.json', [M + 'star_iron_ore'])
tag('data/forge/tags/blocks/ores.json', [M + o for o in ORE_IDS])
tag('data/forge/tags/items/ores.json', [M + o for o in ORE_IDS])
for o in ORE_IDS:
    name = o[:-4]
    tag(f'data/forge/tags/blocks/ores/{name}.json', [M + o])
    tag(f'data/forge/tags/items/ores/{name}.json', [M + o])
tag('data/forge/tags/items/ingots.json', [M + 'mithril_ingot', M + 'star_iron_ingot'])
tag('data/forge/tags/items/ingots/mithril.json', [M + 'mithril_ingot'])
tag('data/forge/tags/items/ingots/star_iron.json', [M + 'star_iron_ingot'])
tag('data/forge/tags/items/gems.json', [M + 'bloodstone', M + 'mana_crystal'])
tag('data/forge/tags/items/gems/bloodstone.json', [M + 'bloodstone'])
tag('data/forge/tags/items/gems/mana_crystal.json', [M + 'mana_crystal'])
tag('data/forge/tags/items/raw_materials.json', [M + 'raw_mithril', M + 'raw_star_iron'])
tag('data/forge/tags/items/raw_materials/mithril.json', [M + 'raw_mithril'])
tag('data/forge/tags/items/raw_materials/star_iron.json', [M + 'raw_star_iron'])


# ====================================================================
#  ЛОКАЛИЗАЦИЯ
# ====================================================================
LANG_RU, LANG_EN = {}, {}


def L(key, ru, en):
    LANG_RU[key] = ru
    LANG_EN[key] = en


L('itemGroup.mechanix', 'Mechanix RPG', 'Mechanix RPG')

# ---- предметы и блоки ----
for iid, ru, en, _, _ in ITEM_DEFS:
    L(f'item.mechanix.{iid}', ru, en)
for name, ru, en in BLOCK_ITEMS:
    L(f'block.mechanix.{name}', ru, en)
L('block.mechanix.moonroot_crop', 'Лунный корень', 'Moonroot')

DESCS = {
    'fire_staff': ('ПКМ: Огненный шар. Тратит ману и прочность.', 'Right-click: Fireball. Uses mana and durability.'),
    'frost_staff': ('ПКМ: Ледяной шип. Тратит ману и прочность.', 'Right-click: Ice Spike. Uses mana and durability.'),
    'lightning_staff': ('ПКМ: Цепная молния. Тратит ману и прочность.', 'Right-click: Chain Lightning. Uses mana and durability.'),
    'healing_staff': ('ПКМ: Лечение. Тратит ману и прочность.', 'Right-click: Heal. Uses mana and durability.'),
    'bloodstone_blade': ('Каждый удар восстанавливает 3 ед. здоровья.', 'Each hit restores 3 health.'),
    'abyss_blade': ('Иссушает врага и возвращает 4 маны за удар.', 'Withers the target and restores 4 mana per hit.'),
    'goblin_dagger': ('25% шанс отравить цель.', '25% chance to poison the target.'),
    'orc_greataxe': ('Удар задевает всех врагов рядом с целью.', 'Hits also damage enemies around the target.'),
    'bonk_bat': ('Отбрасывает цель высоко и далеко. БОНК!', 'Launches the target far and high. BONK!'),
    'mana_potion': ('Восстанавливает 40 маны.', 'Restores 40 mana.'),
    'great_mana_potion': ('Восстанавливает 120 маны.', 'Restores 120 mana.'),
    'experience_tome': ('ПКМ: даёт 40% опыта до следующего уровня.', 'Right-click: grants 40% of the XP needed for the next level.'),
    'rebirth_scroll': ('ПКМ: позволяет выбрать расу заново.', 'Right-click: lets you choose your race again.'),
    'reset_scroll': ('ПКМ: сбрасывает распределённые очки характеристик.', 'Right-click: refunds all spent stat points.'),
    'rpg_guide': ('ПКМ: открыть путеводитель по моду.', 'Right-click: open the mod guide.'),
    'creative_wand': ('Только для творческого режима. ПКМ: уровень вверх, Shift+ПКМ: полная мана.',
                      'Creative mode only. Right-click: level up, Shift+right-click: full mana.'),
}
for iid, (ru, en) in DESCS.items():
    L(f'item.mechanix.{iid}.desc', ru, en)

# ---- мобы ----
for mid, ru, en in [('goblin', 'Гоблин', 'Goblin'), ('orc', 'Орк', 'Orc'), ('skeleton_knight', 'Скелет-рыцарь', 'Skeleton Knight'),
                    ('cave_stalker', 'Пещерный охотник', 'Cave Stalker'), ('goblin_king', 'Король гоблинов', 'Goblin King'),
                    ('orc_warlord', 'Варлорд орков', 'Orc Warlord'), ('lich', 'Лич', 'Lich'),
                    ('spirit_wolf', 'Дух-волк', 'Spirit Wolf')]:
    L(f'entity.mechanix.{mid}', ru, en)

# ---- расы ----
RACES = {
    'human': ('Человек', 'Human',
              'Универсальная раса без слабых мест. Быстро учится.', 'Versatile and balanced, with no weaknesses. A quick learner.',
              '+15% к получаемому опыту.', '+15% experience gained.',
              'Второе дыхание', 'Second Wind',
              'Лечит половину здоровья, снимает яд, иссушение, слабость, замедление и слепоту, даёт регенерацию.',
              'Heals half of your health, removes poison, wither, weakness, slowness and blindness, grants Regeneration.'),
    'elf': ('Эльф', 'Elf',
            'Ловкий и мудрый житель лесов. Много маны, но хрупкое тело.', 'A nimble, wise forest dweller. Lots of mana, but frail.',
            'Регенерация в лесу. Урон от падения вдвое меньше.', 'Regeneration in forests. Half fall damage.',
            'Шаг ветра', 'Windstep',
            'На 10 секунд даёт скорость III, прыгучесть III и плавное падение.', 'For 10 seconds grants Speed III, Jump Boost III and Slow Falling.'),
    'dwarf': ('Гном', 'Dwarf',
              'Крепкий подземный мастер. Медленный, зато живучий и сильный.', 'A sturdy underground master. Slow, but tough and strong.',
              'Под землёй (ниже 60-го уровня, без неба над головой): спешка и ночное зрение.',
              'Underground (below Y 60, no sky above): Haste and Night Vision.',
              'Каменная кожа', 'Stone Skin',
              'На 15 секунд: сопротивление урону II, замедление I и спешка II.', 'For 15 seconds: Resistance II, Slowness I and Haste II.'),
    'beastkin': ('Зверолюд', 'Beastkin',
                 'Быстрый и ловкий охотник с чутьём зверя.', 'A fast, agile hunter with animal instincts.',
                 'Постоянная прыгучесть. Урон от падения вдвое меньше.', 'Permanent Jump Boost. Half fall damage.',
                 'Рывок', 'Pounce',
                 'Мощный рывок вперёд. Первый задетый враг получает урон.', 'A powerful forward dash. The first enemy hit takes damage.'),
    'vampire': ('Вампир', 'Vampire',
                'Ночной хищник. Силён во тьме, но боится солнца.', 'A night predator. Strong in the dark, weak in sunlight.',
                'Горит на солнце без шлема. Ночью видит в темноте. Лечится на 12% от нанесённого урона.',
                'Burns in sunlight without a helmet. Night Vision at night. Heals for 12% of damage dealt.',
                'Укус крови', 'Blood Bite',
                'Кусает врага перед собой (до 5 блоков) и лечится на 80% от урона.', 'Bites the enemy in front (up to 5 blocks) and heals for 80% of the damage.'),
    'demon': ('Демон', 'Demon',
              'Порождение адского пламени. Большой урон.', 'Born of hellfire. Deals extra damage.',
              'Постоянная огнестойкость.', 'Permanent Fire Resistance.',
              'Адское пламя', 'Hellfire Nova',
              'Взрыв пламени в радиусе 7 блоков: урон и поджог на 8 секунд.', 'A burst of flame within 7 blocks: damage and 8 seconds of burning.'),
    'dragonborn': ('Драконид', 'Dragonborn',
                   'Потомок драконов. Сильный, прочный, с чешуёй вместо кожи.', 'A dragon descendant. Strong and sturdy, with scales instead of skin.',
                   'Вдвое меньше урона от огня. Быстрее голодает.', 'Takes half fire damage. Gets hungry faster.',
                   'Дыхание дракона', 'Dragon Breath',
                   'Конус огня впереди на 8 блоков: урон и поджог на 6 секунд.', 'A cone of fire 8 blocks long: damage and 6 seconds of burning.'),
    'orc': ('Орк', 'Orc',
            'Свирепый воин с огромной силой. Мало маны.', 'A fierce warrior of huge strength. Little mana.',
            '+20% урона, когда здоровья меньше половины.', '+20% damage when below half health.',
            'Боевой клич', 'War Cry',
            'Союзники рядом получают силу II на 20 секунд, враги слабеют.', 'Nearby allies get Strength II for 20 seconds, enemies are weakened.'),
    'undead': ('Нежить', 'Undead',
               'Не живой и не мёртвый. Невосприимчив к ядам.', 'Neither alive nor dead. Immune to poisons.',
               'Иммунитет к яду, иссушению и голоду. Раз в 10 минут переживает смертельный удар.',
               'Immune to poison, wither and hunger. Once per 10 minutes survives a fatal hit.',
               'Костяной щит', 'Bone Shield',
               'Мощное поглощение урона на 20 секунд, враги рядом отбрасываются.', 'Strong damage absorption for 20 seconds, nearby enemies are knocked back.'),
    'angel': ('Ангел', 'Angel',
              'Небесное создание. Много маны, лёгкое тело.', 'A heavenly being. Lots of mana and a light body.',
              'Не получает урон от падения.', 'Takes no fall damage.',
              'Небесный полёт', 'Heavenly Flight',
              'Полёт на 15 секунд.', 'Flight for 15 seconds.'),
    'goblin': ('Гоблин', 'Goblin',
               'Мелкий, юркий и хитрый. Слабое здоровье, зато быстрый.', 'Small, quick and cunning. Low health, but fast.',
               '+10% опыта. 15% шанс удвоить добычу с убитых мобов. Урон от падения вдвое меньше.',
               '+10% experience. 15% chance to double mob drops. Half fall damage.',
               'Исчезновение', 'Vanish',
               'Невидимость и скорость III на 8 секунд, враги теряют вас из виду.', 'Invisibility and Speed III for 8 seconds, enemies lose track of you.'),
    'merfolk': ('Русалка', 'Merfolk',
                'Дитя морей. Лучше всего чувствует себя в воде.', 'A child of the sea. Thrives in water.',
                'В воде: подводное дыхание, грация дельфина и ночное зрение.', 'In water: Water Breathing, Dolphin\'s Grace and Night Vision.',
                'Приливная волна', 'Tidal Wave',
                'Волна в радиусе 8 блоков: урон, отбрасывание и замедление. Тушит огонь.', 'A wave within 8 blocks: damage, knockback and slowness. Extinguishes you.'),
    'kitsune': ('Кицунэ', 'Kitsune',
                'Хитрая лисица-волшебница. Очень много маны.', 'A cunning fox mage. A huge mana pool.',
                'Постоянная удача.', 'Permanent Luck.',
                'Лисий шаг', 'Fox Step',
                'Телепорт за спину врага (до 24 блоков), враг слепнет, вы ускоряетесь.', 'Teleports behind an enemy (up to 24 blocks), blinding it and speeding you up.'),
    'werewolf': ('Оборотень', 'Werewolf',
                 'Зверь в человеческом облике. Страшен ночью.', 'A beast in human form. Terrifying at night.',
                 'Ночью: сила, скорость и ночное зрение. Урон от падения вдвое меньше.', 'At night: Strength, Speed and Night Vision. Half fall damage.',
                 'Вой', 'Howl',
                 'Враги в радиусе 10 блоков слабеют и замедляются, вы получаете силу II и скорость II на 15 секунд.',
                 'Enemies within 10 blocks are weakened and slowed; you get Strength II and Speed II for 15 seconds.'),
}
for rid, (rn, en, rd, ed, rp, ep, ra, ea, rad, ead) in RACES.items():
    L(f'race.mechanix.{rid}', rn, en)
    L(f'race.mechanix.{rid}.desc', rd, ed)
    L(f'race.mechanix.{rid}.passive', rp, ep)
    L(f'race.mechanix.{rid}.ability', ra, ea)
    L(f'race.mechanix.{rid}.ability.desc', rad, ead)

# ---- классы ----
CLASSES = {
    'warrior': ('Воин', 'Warrior', 'Мастер ближнего боя. Сила и крепость.', 'A melee master. Strength and toughness.'),
    'mage': ('Маг', 'Mage', 'Повелитель стихий. Огромный урон на расстоянии.', 'A master of the elements. Huge ranged damage.'),
    'hunter': ('Охотник', 'Hunter', 'Меткий стрелок со зверями-помощниками.', 'A sharp archer with beast companions.'),
    'assassin': ('Убийца', 'Assassin', 'Скрытность, скорость и удары в спину.', 'Stealth, speed and backstabs.'),
    'healer': ('Целитель', 'Healer', 'Лечит союзников и карает нежить.', 'Heals allies and smites the undead.'),
}
for cid, (rn, en, rd, ed) in CLASSES.items():
    L(f'class.mechanix.{cid}', rn, en)
    L(f'class.mechanix.{cid}.desc', rd, ed)

# ---- заклинания ----
SPELLS = {
    'leap_strike': ('Прыжковый удар', 'Leap Strike', 'Рывок вперёд, первый задетый враг получает урон.', 'Dash forward, the first enemy hit takes damage.'),
    'shatter': ('Сокрушение', 'Shatter', 'Удар по земле: урон и отбрасывание врагов вокруг (5 блоков).', 'Ground slam: damages and knocks back enemies within 5 blocks.'),
    'battle_cry': ('Боевой клич', 'Battle Cry', 'Союзники рядом получают силу II и сопротивление урону на 10 секунд.', 'Nearby allies get Strength II and Resistance for 10 seconds.'),
    'iron_will': ('Железная воля', 'Iron Will', 'Сильное сопротивление урону на 5 секунд и поглощение.', 'Strong damage resistance for 5 seconds plus absorption.'),
    'blade_storm': ('Буря клинков', 'Blade Storm', 'Вихрь клинков: большой урон врагам вокруг (4 блока).', 'A whirl of blades: heavy damage to enemies within 4 blocks.'),
    'fireball': ('Огненный шар', 'Fireball', 'Взрыв огня там, куда вы смотрите. Поджигает врагов.', 'A burst of fire where you look. Sets enemies ablaze.'),
    'ice_spike': ('Ледяной шип', 'Ice Spike', 'Урон и сильное замедление, враг покрывается льдом.', 'Damage and heavy slowness; the target freezes.'),
    'chain_lightning': ('Цепная молния', 'Chain Lightning', 'Бьёт цель и перескакивает на ближайших врагов (до 4 целей).', 'Strikes a target and jumps to nearby enemies (up to 4 targets).'),
    'mana_shield': ('Щит маны', 'Mana Shield', 'Мощное поглощение урона на 20 секунд.', 'Strong damage absorption for 20 seconds.'),
    'meteor': ('Метеор', 'Meteor', 'Удар с небес по области: огромный урон, поджог и отбрасывание.', 'A strike from the sky on an area: huge damage, burning and knockback.'),
    'precise_shot': ('Меткий выстрел', 'Precise Shot', 'Мгновенный выстрел на 50 блоков.', 'An instant shot reaching 50 blocks.'),
    'net_trap': ('Сеть', 'Net Trap', 'Почти полностью обездвиживает врагов в области и ослабляет их.', 'Nearly immobilises enemies in an area and weakens them.'),
    'summon_spirit': ('Призыв духов', 'Summon Spirit', 'Призывает двух духов-волков на 45 секунд.', 'Summons two spirit wolves for 45 seconds.'),
    'beast_run': ('Бег зверя', 'Beast Run', 'Скорость IV и прыгучесть III на 8 секунд.', 'Speed IV and Jump Boost III for 8 seconds.'),
    'arrow_rain': ('Ливень стрел', 'Arrow Rain', 'Град стрел по области: урон и замедление.', 'A hail of arrows over an area: damage and slowness.'),
    'shadow_step': ('Шаг в тень', 'Shadow Step', 'Телепорт за спину врага, удар и короткая невидимость.', 'Teleport behind an enemy, strike it and turn briefly invisible.'),
    'backstab': ('Удар в спину', 'Backstab', 'Двойной урон, если враг смотрит в другую сторону.', 'Double damage if the enemy is facing away.'),
    'smoke_bomb': ('Дымовая бомба', 'Smoke Bomb', 'Враги слепнут и теряют вас, вы становитесь невидимым и быстрым.', 'Enemies are blinded and lose you; you turn invisible and fast.'),
    'venom_strike': ('Ядовитый удар', 'Venom Strike', 'Сильный яд и иссушение на цели.', 'Heavy poison and wither on the target.'),
    'death_mark': ('Метка смерти', 'Death Mark', 'Цель на 20 секунд получает на 35% больше урона.', 'The target takes 35% more damage for 20 seconds.'),
    'heal': ('Лечение', 'Heal', 'Лечит вас и союзников рядом.', 'Heals you and nearby allies.'),
    'cleanse': ('Очищение', 'Cleanse', 'Снимает вредные эффекты с вас и союзников.', 'Removes harmful effects from you and your allies.'),
    'holy_light': ('Священный свет', 'Holy Light', 'Лечит союзников, жжёт врагов, особенно нежить.', 'Heals allies and burns enemies, especially the undead.'),
    'blessing': ('Благословение', 'Blessing', 'Регенерация, сила и сопротивление урону для союзников.', 'Regeneration, Strength and Resistance for allies.'),
    'sanctuary': ('Святилище', 'Sanctuary', 'Союзники рядом получают сильное сопротивление урону.', 'Nearby allies gain strong damage resistance.'),
}
for sid, (rn, en, rd, ed) in SPELLS.items():
    L(f'spell.mechanix.{sid}', rn, en)
    L(f'spell.mechanix.{sid}.desc', rd, ed)

# ---- характеристики, клавиши ----
for k, ru, en in [('str', 'Сила', 'Strength'), ('agi', 'Ловкость', 'Agility'), ('int', 'Интеллект', 'Intelligence'),
                  ('vit', 'Живучесть', 'Vitality')]:
    L(f'stat.mechanix.{k}', ru, en)
for i in range(1, 5):
    L(f'key.mechanix.spell{i}', f'Заклинание {i}', f'Spell {i}')
L('key.mechanix.ability', 'Способность расы', 'Racial ability')
L('key.mechanix.menu', 'Меню персонажа', 'Character menu')
L('key.categories.mechanix', 'Mechanix RPG', 'Mechanix RPG')

# ---- сообщения ----
for k, ru, en in [
    ('levelup', 'Новый уровень: %s!', 'Level up: %s!'),
    ('race_chosen', 'Ваша раса: %s', 'Your race: %s'),
    ('cooldown', 'Перезарядка: %s с', 'Cooldown: %s s'),
    ('no_mana', 'Не хватает маны!', 'Not enough mana!'),
    ('no_target', 'Нет цели!', 'No target!'),
    ('empty_slot', 'Слот пуст. Изучите заклинание в меню (K).', 'Empty slot. Learn a spell in the menu (K).'),
    ('no_race', 'Сначала выберите расу.', 'Choose a race first.'),
    ('backstab', 'Удар в спину!', 'Backstab!'),
    ('last_breath', 'Последний вздох!', 'Last Breath!'),
    ('creative_only', 'Только для творческого режима.', 'Creative mode only.'),
    ('goblin_king.phase1', 'Король гоблинов: «Взять их, мои гоблины!»', 'Goblin King: "Get them, my goblins!"'),
    ('goblin_king.phase2', 'Король гоблинов: «Вы разозлили меня!»', 'Goblin King: "You have angered me!"'),
    ('goblin_king.phase3', 'Король гоблинов: «Моё золото! Моя корона!»', 'Goblin King: "My gold! My crown!"'),
    ('orc_warlord.phase1', 'Варлорд орков: «Сразитесь со мной!»', 'Orc Warlord: "Face me!"'),
    ('orc_warlord.phase2', 'Варлорд орков: «Я только начал!»', 'Orc Warlord: "I have only begun!"'),
    ('orc_warlord.phase3', 'Варлорд орков: «ЗА ВОЙНУ!»', 'Orc Warlord: "FOR WAR!"'),
    ('lich.phase1', 'Лич: «Вы пришли умереть.»', 'Lich: "You have come to die."'),
    ('lich.phase2', 'Лич: «Тьма поглотит вас.»', 'Lich: "Darkness will consume you."'),
    ('lich.phase3', 'Лич: «Я не могу умереть!»', 'Lich: "I cannot die!"'),
]:
    L(f'msg.mechanix.{k}', ru, en)

# ---- интерфейс ----
for k, ru, en in [
    ('choose_race', 'Выберите расу', 'Choose your race'),
    ('confirm', 'Подтвердить', 'Confirm'),
    ('character', 'Персонаж', 'Character'),
    ('close', 'Закрыть', 'Close'),
    ('learn', 'Изучить', 'Learn'),
    ('lvl', 'Ур.', 'Lv.'),
    ('hud_points', 'Очки: %s | Навыки: %s', 'Points: %s | Skills: %s'),
    ('points', 'Очки характеристик: %s | Очки навыков: %s', 'Stat points: %s | Skill points: %s'),
    ('xp_line', 'Опыт: %s / %s', 'XP: %s / %s'),
    ('no_class', 'Без класса', 'No class'),
    ('choose_class', 'Выберите класс', 'Choose a class'),
    ('class_locked', 'Класс открывается на 3 уровне', 'Classes unlock at level 3'),
    ('spells', 'Заклинания', 'Spells'),
    ('spell_info', 'Ур. %s · Мана %s · Перезарядка %s с', 'Lv. %s · Mana %s · Cooldown %s s'),
    ('passive', 'Пассивно:', 'Passive:'),
    ('ability', 'Способность (R): %s', 'Ability (R): %s'),
    ('cooldown', 'Перезарядка: %s с', 'Cooldown: %s s'),
    ('health', 'Здоровье', 'Health'),
    ('speed', 'Скорость', 'Speed'),
    ('mana', 'Мана', 'Mana'),
    ('xp', 'Опыт', 'XP'),
]:
    L(f'gui.mechanix.{k}', ru, en)

# ---- гайд (8 страниц) ----
GUIDE_RU = [
    'Добро пожаловать!\n\nПри первом входе в мир выберите расу. Раса даёт бонусы, пассивный навык и способность на клавише R.\n\nСменить расу можно Свитком перерождения.',
    'Уровни и опыт\n\nОпыт даёт убийство монстров. Новые уровни дают очки характеристик.\n\nМеню персонажа: клавиша K. Там же изучаются заклинания.',
    'Характеристики\n\nСила: урон.\nЛовкость: скорость и скорость атаки.\nИнтеллект: мана и магия.\nЖивучесть: здоровье и броня.\n\nСбросить очки: Свиток сброса.',
    'Классы\n\nС 3 уровня: Воин, Маг, Охотник, Убийца или Целитель. У каждого класса по 5 заклинаний, они открываются по мере роста уровня.',
    'Заклинания и мана\n\nПоставьте изученные заклинания на клавиши Z, X, C и V. Каждое тратит ману и потом перезаряжается.\n\nМану восстанавливают зелья маны. Посохи колдуют своё заклинание.',
    'Руды и броня\n\nГлубоко под землёй: мифрил, звёздное железо, кровавый камень и кристаллы маны. Из них делают инструменты, броню и посохи.\n\nПолный комплект брони даёт бонус.',
    'Структуры и боссы\n\nБашня мага, лагерь гоблинов и руины орков. Внутри их ждут Король гоблинов, Варлорд орков и Лич.\n\nБоссы дерутся в 3 фазы и дают редкую добычу.',
    'Для операторов\n\nКоманды:\n/mechanix level, xp, points, race, class, mana, reset\n\nНастройки: config/mechanix-common.toml\n\nПриятной игры!',
]
GUIDE_EN = [
    'Welcome!\n\nOn your first join, choose a race. It gives bonuses, a passive skill and an ability on the R key.\n\nChange your race with a Rebirth Scroll.',
    'Levels and XP\n\nKilling monsters gives experience. Each level grants stat points.\n\nCharacter menu: K key. Learn spells there too.',
    'Stats\n\nStrength: damage.\nAgility: speed and attack speed.\nIntelligence: mana and magic.\nVitality: health and armor.\n\nRefund points: Reset Scroll.',
    'Classes\n\nFrom level 3: Warrior, Mage, Hunter, Assassin or Healer. Each class has 5 spells that unlock as you level up.',
    'Spells and mana\n\nBind learned spells to Z, X, C and V. Each costs mana, then goes on cooldown.\n\nMana potions restore mana. Staves cast their own spell.',
    'Ores and armor\n\nDeep underground: mithril, star iron, bloodstone and mana crystals. Craft tools, armor and staves from them.\n\nA full armor set gives a bonus.',
    'Structures and bosses\n\nMage Tower, Goblin Camp and Orc Ruins. Inside wait the Goblin King, the Orc Warlord and the Lich.\n\nBosses fight in 3 phases and drop rare loot.',
    'For operators\n\nCommands:\n/mechanix level, xp, points, race, class, mana, reset\n\nConfig: config/mechanix-common.toml\n\nHave fun!',
]
for i in range(8):
    L(f'guide.mechanix.page{i + 1}', GUIDE_RU[i], GUIDE_EN[i])

w(ASSETS / 'lang/ru_ru.json', LANG_RU)
w(ASSETS / 'lang/en_us.json', LANG_EN)
print(f'Готово: {len(ITEM_DEFS)} предметов, {len(LANG_RU)} строк перевода.')
