"""Servidor e regras do RPG Fazenda do Recomeço (somente biblioteca padrão)."""

from __future__ import annotations

import json
from http import HTTPStatus
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from threading import Lock
from urllib.parse import urlparse

ROOT = Path(__file__).parent
PLOTS = [(112, 72), (128, 72), (144, 72), (160, 72), (112, 88), (128, 88),
         (144, 88), (160, 88), (112, 104), (128, 104), (144, 104), (160, 104)]
CHARACTERS = [
    {"id": "lia", "name": "Lia", "x": 191, "y": 79, "palette": "rose"},
    {"id": "bento", "name": "Bento", "x": 294, "y": 143, "palette": "blue"},
]


class Farm:
    """Fonte única de verdade das regras e do estado da fazenda."""

    def __init__(self) -> None:
        self.reset()

    def reset(self) -> None:
        self.player = [61, 113]
        self.day, self.wood, self.seeds, self.crops = 1, 8, 7, 0
        self.house, self.barn = 0, 0
        self.plots = [0] * len(PLOTS)
        self.dialogue = None
        self.talked_to = set()
        self.message = "Uma fazenda esquecida... e um novo começo."

    def state(self) -> dict:
        return {"player": self.player, "day": self.day, "wood": self.wood,
                "seeds": self.seeds, "crops": self.crops, "house": self.house,
                "barn": self.barn, "plots": self.plots, "message": self.message,
                "characters": CHARACTERS, "dialogue": self.dialogue,
                "quest": self.quest()}

    def say(self, text: str) -> None:
        self.dialogue = None
        self.message = text

    def quest(self) -> str:
        if self.house == 0:
            return f"Reconstrua seu lar ({self.wood}/6 madeira)"
        if self.barn == 0:
            return f"Construa o galinheiro ({self.wood}/5 madeira)"
        if self.barn == 1:
            return f"Prepare o redil ({self.wood}/9 madeira, {self.crops}/2 colheitas)"
        return "A fazenda voltou a ter vida!"

    def near(self, x: int, y: int) -> bool:
        return abs(self.player[0] - x) < 18 and abs(self.player[1] - y) < 18

    def move(self, dx: int, dy: int) -> None:
        x, y = self.player[0] + dx, self.player[1] + dy
        blocked = (23 < x < 90 and 44 < y < 89) or (self.house and 205 < x < 276 and 91 < y < 151)
        if 12 < x < 302 and 38 < y < 162 and not blocked:
            self.player = [x, y]

    def next_day(self) -> None:
        self.day += 1
        self.plots = [3 if plot == 2 else plot for plot in self.plots]
        self.say(f"Dia {self.day}: a terra continua florescendo.")

    def talk(self, character: dict) -> None:
        name = character["name"]
        first_time = character["id"] not in self.talked_to
        self.talked_to.add(character["id"])
        if character["id"] == "lia":
            lines = (["Você veio mesmo ficar? Esta terra estava silenciosa há anos.",
                      "O lago guarda tábuas trazidas pela corrente. Elas podem salvar sua casa."]
                     if first_time else
                     ["Cada semente é uma promessa, fazendeiro.", "Quando a casa estiver pronta, ela terá uma luz na janela."])
        else:
            lines = (["Ouvi martelos na velha fazenda. Bom ouvir vida por aqui.",
                      "Galinhas gostam de abrigo. O celeiro ainda parece firme."]
                     if first_time else
                     ["As ovelhas vão gostar do campo que você está cuidando.", "Uma boa fazenda é feita de paciência e gentileza."])
        self.dialogue = {"speaker": name, "palette": character["palette"], "lines": lines, "line": 0}
        self.message = f"{name} quer conversar."

    def advance_dialogue(self) -> bool:
        if not self.dialogue:
            return False
        self.dialogue["line"] += 1
        if self.dialogue["line"] >= len(self.dialogue["lines"]):
            self.dialogue = None
            self.message = "A conversa trouxe um pouco de calor à fazenda."
        return True

    def interact(self) -> None:
        if self.advance_dialogue():
            return
        for character in CHARACTERS:
            if self.near(character["x"], character["y"]):
                self.talk(character)
                return
        if self.near(55, 52):
            if self.house == 0 and self.wood >= 6:
                self.wood -= 6; self.house = 1; self.say("Uma cabana simples. Agora existe um lar.")
            elif self.house == 1 and self.wood >= 10 and self.crops >= 3:
                self.wood -= 10; self.crops -= 3; self.house = 2; self.say("A velha casa virou um lar acolhedor!")
            else: self.say("Seu lar está completo." if self.house == 2 else "Oficina: 6 madeiras para construir a cabana.")
            return
        if self.near(262, 58):
            if self.barn == 0 and self.wood >= 5:
                self.wood -= 5; self.barn = 1; self.say("O galinheiro ganhou vida. Cocoricó!")
            elif self.barn == 1 and self.wood >= 9 and self.crops >= 2:
                self.wood -= 9; self.crops -= 2; self.barn = 2; self.say("Um redil novo para as ovelhas. Bééé!")
            else: self.say("Os animais parecem felizes." if self.barn == 2 else "Celeiro: traga mais recursos para crescer.")
            return
        if self.near(78, 145):
            self.wood += 2; self.say("Você pescou materiais no lago. +2 madeira"); return
        for index, (x, y) in enumerate(PLOTS):
            if self.near(x + 8, y + 8):
                stage = self.plots[index]
                if stage == 0: self.plots[index] = 1; self.say("Você arou a terra. Interaja de novo para plantar.")
                elif stage == 1 and self.seeds: self.plots[index] = 2; self.seeds -= 1; self.say("Sementes plantadas. Espere o próximo dia.")
                elif stage == 2: self.say("O broto foi cuidado. Volte no próximo dia.")
                elif stage == 3:
                    self.plots[index] = 0; self.crops += 1; self.wood += 1; self.seeds += 1
                    self.say("Colheita farta! +1 alimento, +1 madeira, +1 semente")
                else: self.say("Sem sementes. Colha uma plantação madura primeiro.")
                return
        self.say("Nada por aqui. Explore a fazenda!")

    def action(self, data: dict) -> dict:
        action = data.get("action")
        if action == "move": self.move(int(data.get("dx", 0)), int(data.get("dy", 0)))
        elif action == "interact": self.interact()
        elif action == "day": self.next_day()
        elif action == "reset": self.reset()
        else: self.say("Ação desconhecida.")
        return self.state()


farm, lock = Farm(), Lock()


class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(ROOT), **kwargs)

    def do_GET(self):
        if urlparse(self.path).path == "/api/state":
            with lock: self.send_json(farm.state())
        else: super().do_GET()

    def do_POST(self):
        if urlparse(self.path).path != "/api/action":
            self.send_error(HTTPStatus.NOT_FOUND); return
        try:
            length = int(self.headers.get("Content-Length", "0"))
            data = json.loads(self.rfile.read(length) or b"{}")
            with lock: result = farm.action(data)
            self.send_json(result)
        except (ValueError, json.JSONDecodeError):
            self.send_error(HTTPStatus.BAD_REQUEST, "JSON inválido")

    def send_json(self, payload: dict) -> None:
        body = json.dumps(payload).encode()
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers(); self.wfile.write(body)


if __name__ == "__main__":
    print("Fazenda do Recomeço em http://localhost:8000")
    ThreadingHTTPServer(("", 8000), Handler).serve_forever()
