var evtSource = new EventSource("ChatServlet");
var idMensaje = 'General';
evtSource.onmessage = function(e){
    var data = JSON.parse(e.data);
    if(data.idMensaje==idMensaje){
        console.log(data.text);
        var received = data.user + ":  "+data.text +"\n";
        document.getElementById('mensajes').value+=(received);
    }
};

evtSource.addEventListener('nuevoTema', function(e){
    var data = JSON.parse(e.data);
    var nuevoTema = document.createElement('div');
    nuevoTema.setAttribute('class', "temaChat");
    nuevoTema.setAttribute('id', data.nombreTema);
    nuevoTema.setAttribute('onclick', "idMensaje="+"\""+data.nombreTema+"\"; limpiar();");
    nuevoTema.appendChild(document.createTextNode(data.nombreTema));
    var ultimo_div = document.getElementById("nuevoTemaConversacion");
    document.getElementsByClassName('temas')[0].insertBefore(nuevoTema,ultimo_div);
});

evtSource.addEventListener('quitarTema', function(e){
    var data = JSON.parse(e.data);
    console.log("\""+data.nombreTema+"\"");
    document.getElementById(data.nombreTema).parentNode.removeChild(document.getElementById(data.nombreTema));
});

document.getElementById("enviar").addEventListener('click', function (){
    enviarMensaje();
});

document.getElementById("enviarTema").addEventListener('click', function (){
    if(document.getElementById("nuevoTema").value.length>3){
        var tmp = document.getElementById(document.getElementById("nuevoTema").value);
        if(tmp==undefined || tmp==null){
            var request  = new XMLHttpRequest();
            request.addEventListener('load',function (){
                console.log('Message sent!');
            });

           request.open('POST','ChatServlet',true);
           request.setRequestHeader("Content-type","application/x-www-form-urlencoded; charset=utf-8");
           request.send("tema="+document.getElementById('nuevoTema').value+"&tipo=nuevoTema");
           document.getElementById('nuevoTema').value = "";
        }else{
           alert("Ya existe un tema con el nombre ingresado");
        }
    }else{
        alert("El nombre no es valido mínimo tres caracteres");
    }
});

function limpiar(){
    document.getElementsByClassName("cabecera")[0].innerHTML = "Actualmente estas en el grupo: "+ idMensaje;
    document.getElementById('mensajes').value = "";
}

document.getElementById('emojics').addEventListener('click', function (){
    document.getElementById("divEmo").style.display= "block";
});

for(var i = 0; i<document.getElementsByTagName("p").length; i++){
    document.getElementsByTagName("p")[i].addEventListener('click', function (e){
        document.getElementById("texto").value += this.id;
        document.getElementById("divEmo").style.display = "none";
    });
}

document.getElementById("texto").addEventListener("keyup", function (e){
    if(e.keyCode == 13){
        enviarMensaje();
    }else{
//        document.getElementById("texto").value;
    }
        
});

function enviarMensaje(){
    var request  = new XMLHttpRequest();
    request.addEventListener('load',function (){
        console.log('Message sent!');
    });

   request.open('POST','ChatServlet',true);
   request.setRequestHeader("Content-type","application/x-www-form-urlencoded; charset=utf-8");
   request.send("user="+document.getElementById('username').value+"&text="+document.getElementById('texto').value+"&tema="+idMensaje);
   document.getElementById('texto').value = "";
}
