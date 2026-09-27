const canvas = document.querySelector("#game");
const ctx = canvas.getContext("2d");
const message = document.querySelector("#message");
ctx.imageSmoothingEnabled = false;

const S = 3;
const PLOTS = [[112, 72], [128, 72], [144, 72], [160, 72], [112, 88], [128, 88],
  [144, 88], [160, 88], [112, 104], [128, 104], [144, 104], [160, 104]];
const MOVES = { ArrowUp: [0, -2], w: [0, -2], ArrowDown: [0, 2], s: [0, 2],
  ArrowLeft: [-2, 0], a: [-2, 0], ArrowRight: [2, 0], d: [2, 0] };
let state, facing = "down", walking = false, frame = 0, busy = false;

function rect(x, y, width, height, color) { ctx.fillStyle = color; ctx.fillRect(x * S, y * S, width * S, height * S); }
function text(value, x, y, size = 6, color = "#fff4cf") { ctx.fillStyle = color; ctx.font = `${size * S}px monospace`; ctx.fillText(value, x * S, y * S); }
function wrappedText(value, x, y, size, maxChars, color = "#fff4cf") {
  const words = value.split(" "); let line = ""; let row = 0;
  words.forEach(word => { const next = `${line} ${word}`.trim(); if (next.length > maxChars) { text(line, x, y + row * (size + 2), size, color); line = word; row++; } else line = next; });
  text(line, x, y + row * (size + 2), size, color);
}
function shadow(x, y, width = 10) { rect(x, y, width, 2, "#405b4d"); }

async function action(payload) {
  if (busy) return;
  busy = true;
  try {
    const response = await fetch("/api/action", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
    state = await response.json();
    draw();
  } catch (_) { message.textContent = "Não foi possível falar com o servidor Python."; }
  finally { busy = false; }
}

async function init() { state = await (await fetch("/api/state")).json(); draw(); requestAnimationFrame(animate); }
function animate() { frame++; draw(); requestAnimationFrame(animate); }

function tree(x, y) {
  shadow(x + 3, y + 26, 13); rect(x + 6, y + 14, 5, 13, "#624127");
  rect(x, y + 5, 17, 14, "#245d38"); rect(x + 3, y, 11, 23, "#307643"); rect(x + 5, y + 2, 7, 7, "#62a84f");
}
function character(x, y, palette, isPlayer = false) {
  const bob = walking && isPlayer ? (Math.floor(frame / 7) % 2) : Math.floor(frame / 35) % 2;
  const shirt = isPlayer ? "#487eb5" : palette === "rose" ? "#c95d75" : "#4b84b6";
  const hair = isPlayer ? "#402b25" : palette === "rose" ? "#653442" : "#8a5635";
  shadow(x + 2, y + 16, 10); rect(x + 3, y + 1 + bob, 8, 8, "#242b31");
  rect(x + 4, y + 3 + bob, 6, 6, "#f2bf94"); rect(x + 3, y + 10 + bob, 8, 5, shirt); rect(x + 2, y + bob, 10, 4, hair);
  rect(x + 3, y + 15 + bob, 3, 3, "#263248"); rect(x + 8, y + 15 + (walking ? 1 - bob : bob), 3, 3, "#263248");
  if (!isPlayer) { rect(x + 5, y + 5 + bob, 1, 1, "#31272a"); rect(x + 9, y + 5 + bob, 1, 1, "#31272a"); }
}
function chicken(x, y) { shadow(x, y + 7, 8); rect(x, y, 7, 6, "#fff8e6"); rect(x + 5, y - 2, 2, 2, "#e14d2f"); rect(x + 7, y + 2, 2, 2, "#efae2d"); }
function sheep(x, y) { shadow(x + 1, y + 11, 12); rect(x, y, 12, 8, "#efefde"); rect(x + 9, y + 2, 4, 5, "#272d2c"); rect(x + 2, y + 8, 2, 3, "#272d2c"); rect(x + 8, y + 8, 2, 3, "#272d2c"); }
function sign(x, y, label) { rect(x + 10, y, 2, 9, "#5c3f26"); rect(x, y, 25, 7, "#ecd28d"); text(label, x + 1, y + 5, 3, "#272d2c"); }
function speechMark(x, y) { rect(x, y, 8, 6, "#fff4cf"); rect(x + 2, y + 6, 2, 2, "#fff4cf"); rect(x + 2, y + 2, 1, 1, "#33404a"); rect(x + 5, y + 2, 1, 1, "#33404a"); }

function background() {
  rect(0, 0, 320, 180, "#74b15b");
  for (let y = 40; y < 180; y += 16) for (let x = (Math.floor(y / 16) % 2) * 7; x < 320; x += 29) { rect(x, y + 6, 1, 3, "#4b8d4c"); rect(x + 2, y + 5, 1, 4, "#4b8d4c"); }
  rect(0, 30, 320, 8, "#ebba70"); rect(0, 35, 320, 3, "#c79254");
  rect(15, 126, 71, 40, "#2d7399"); rect(18, 130, 65, 2, "#6ab6c8"); rect(25, 137, 18, 2, "#6ab6c8"); rect(53, 151, 21, 2, "#6ab6c8");
}
function buildings() {
  rect(28, 54, 54, 32, "#584234"); rect(23, 48, 65, 10, "#3e312b"); rect(34, 62, 10, 23, "#976641"); rect(64, 62, 10, 23, "#976641"); rect(49, 70, 12, 16, "#272d2c"); sign(48, 92, "OFICINA");
  rect(232, 58, 54, 37, "#904131"); rect(228, 52, 62, 10, "#5e3530"); rect(254, 74, 10, 21, "#f4d38e"); sign(240, 101, "CELEIRO");
  if (state.house) { rect(210, 120, 58, 37, "#cd8f4d"); rect(205, 112, 68, 13, "#7f3d32"); rect(233, 137, 12, 20, "#553a2b"); rect(217, 133, 10, 8, "#82cadb"); rect(252, 133, 10, 8, "#82cadb"); if (state.house === 2) { rect(237, 139, 4, 4, "#f0d46a"); rect(257, 104, 5, 12, "#be4332"); } }
}
function fields() { state.plots.forEach((stage, index) => { const [x, y] = PLOTS[index]; rect(x, y, 14, 14, stage ? "#74462a" : "#7c9c4b"); if (stage) { rect(x + 2, y + 4, 10, 1, "#4e3523"); rect(x + 1, y + 10, 11, 1, "#4e3523"); } if (stage > 1) { const color = stage === 3 ? "#eec33f" : "#3d8640"; rect(x + 6, y + 4, 2, 8, color); rect(x + 3, y + 6, 3, 2, color); rect(x + 8, y + 5, 3, 2, color); } }); }
function ui() {
  rect(6, 5, 308, 20, "#1c2a2b"); text("FAZENDA DO RECOMEÇO", 12, 14, 7); text(`DIA ${state.day}   MADEIRA ${state.wood}   SEMENTES ${state.seeds}   COLHEITA ${state.crops}`, 12, 22, 6);
  rect(8, 40, 98, 12, "#213739"); text("META: " + state.quest, 11, 48, 4, "#ffe2a2");
  message.textContent = state.message;
  if (state.dialogue) { const d = state.dialogue; rect(12, 116, 296, 47, "#17272bec"); rect(12, 116, 296, 2, "#f2cf89"); character(20, 127, d.palette); text(d.speaker.toUpperCase(), 40, 129, 5, "#f5d58f"); wrappedText(d.lines[d.line], 40, 140, 5, 34); text("E para continuar", 40, 157, 4, "#a9c993"); }
}
function draw() {
  if (!state) return;
  background(); fields(); buildings(); [[93, 49], [188, 55], [294, 104], [187, 151]].forEach(p => tree(...p));
  if (state.barn) { chicken(244, 113); chicken(274, 121); } if (state.barn === 2) sheep(256, 136);
  state.characters.forEach(npc => { character(npc.x, npc.y, npc.palette); if (Math.abs(state.player[0] - npc.x) < 24 && Math.abs(state.player[1] - npc.y) < 24) speechMark(npc.x + 3, npc.y - 9); });
  character(...state.player, "", true); ui();
}

window.addEventListener("keydown", event => {
  const key = event.key.toLowerCase();
  if (MOVES[event.key] || MOVES[key]) { event.preventDefault(); const [dx, dy] = MOVES[event.key] || MOVES[key]; facing = dx ? (dx > 0 ? "right" : "left") : (dy > 0 ? "down" : "up"); walking = true; action({ action: "move", dx, dy }); setTimeout(() => { walking = false; }, 90); }
  else if (key === " " || key === "e") { event.preventDefault(); action({ action: "interact" }); }
  else if (key === "n") action({ action: "day" }); else if (key === "r") action({ action: "reset" });
});
init();
