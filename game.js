const canvas = document.querySelector("#game"), ctx = canvas.getContext("2d"), message = document.querySelector("#message");
ctx.imageSmoothingEnabled = false;
let state;
const S = 3, plotPositions = [[112,72],[128,72],[144,72],[160,72],[112,88],[128,88],[144,88],[160,88],[112,104],[128,104],[144,104],[160,104]];
const rect = (x,y,w,h,c) => { ctx.fillStyle=c; ctx.fillRect(x*S,y*S,w*S,h*S); };
async function action(payload) { const response = await fetch("/api/action", {method:"POST", headers:{"Content-Type":"application/json"}, body:JSON.stringify(payload)}); state = await response.json(); draw(); }
async function init() { state = await (await fetch("/api/state")).json(); draw(); }
function text(value,x,y,size=6,color="#fff4cf") { ctx.fillStyle=color; ctx.font=`${size*S}px monospace`; ctx.fillText(value,x*S,y*S); }
function tree(x,y) { rect(x+6,y+14,5,13,"#5c3f26");rect(x,y+4,17,15,"#2f713d");rect(x+3,y,11,23,"#2f713d");rect(x+4,y+2,8,7,"#438b42"); }
function player(x,y) { rect(x+3,y+2,8,8,"#272d2c");rect(x+4,y+3,6,6,"#efbf8d");rect(x+3,y+10,8,5,"#4b64a4");rect(x+2,y,10,4,"#433128"); }
function sign(x,y,label) { rect(x+10,y,2,9,"#5c3f26");rect(x,y,24,7,"#e4cd8b");text(label,x+1,y+5,3,"#272d2c"); }
function draw() {
  rect(0,0,320,180,"#74b15b"); for(let y=40;y<180;y+=16) for(let x=(Math.floor(y/16)%2)*7;x<320;x+=29){rect(x,y+6,1,3,"#4a8d4c");rect(x+2,y+5,1,4,"#4a8d4c");}
  rect(0,30,320,8,"#ebba70");rect(0,35,320,3,"#c79254");
  rect(15,126,71,40,"#2e7397");rect(25,137,18,2,"#59a8bf");rect(53,151,21,2,"#59a8bf");rect(34,160,13,2,"#59a8bf");
  rect(28,54,54,32,"#584234");rect(23,48,65,10,"#3e312b");rect(34,62,10,23,"#976641");rect(64,62,10,23,"#976641");rect(49,70,12,16,"#272d2c");sign(48,92,"OFICINA");
  state.plots.forEach((stage,i)=>{const [x,y]=plotPositions[i];rect(x,y,14,14,stage?"#74462a":"#7c9c4b"); if(stage){rect(x+2,y+4,10,1,"#4e3523");rect(x+1,y+10,11,1,"#4e3523");} if(stage>1){const c=stage===3?"#eec33f":"#3d8640";rect(x+6,y+4,2,8,c);rect(x+3,y+6,3,2,c);rect(x+8,y+5,3,2,c);}});
  rect(232,58,54,37,"#904131");rect(228,52,62,10,"#5e3530");rect(254,74,10,21,"#f4d38e");sign(240,101,"CELEIRO");
  if(state.barn){ chicken(244,113);chicken(274,121); } if(state.barn===2) sheep(256,136);
  if(state.house){rect(210,120,58,37,"#cd8f4d");rect(205,112,68,13,"#7f3d32");rect(233,137,12,20,"#553a2b");rect(217,133,10,8,"#82cadb");rect(252,133,10,8,"#82cadb"); if(state.house===2){rect(237,139,4,4,"#f0d46a");rect(257,104,5,12,"#be4332");}}
  [[93,49],[188,55],[294,104],[187,151]].forEach(p=>tree(...p)); player(...state.player);
  rect(6,5,308,20,"#1c2a2b");text("FAZENDA DO RECOMEÇO",12,14,7);text(`DIA ${state.day}   MADEIRA ${state.wood}   SEMENTES ${state.seeds}   COLHEITA ${state.crops}`,12,22,6); message.textContent=state.message;
}
function chicken(x,y){rect(x,y,7,6,"#fff");rect(x+5,y-2,2,2,"#e14d2f");rect(x+7,y+2,2,2,"#efae2d");} function sheep(x,y){rect(x,y,12,8,"#efefde");rect(x+9,y+2,4,5,"#272d2c");rect(x+2,y+8,2,3,"#272d2c");rect(x+8,y+8,2,3,"#272d2c");}
const moves={ArrowUp:[0,-2],w:[0,-2],ArrowDown:[0,2],s:[0,2],ArrowLeft:[-2,0],a:[-2,0],ArrowRight:[2,0],d:[2,0]};
window.addEventListener("keydown", e=>{const key=e.key; if(moves[key]) {e.preventDefault();action({action:"move",dx:moves[key][0],dy:moves[key][1]});} else if(key===" "||key.toLowerCase()==="e"){e.preventDefault();action({action:"interact"});} else if(key.toLowerCase()==="n")action({action:"day"}); else if(key.toLowerCase()==="r")action({action:"reset"});}); init();
