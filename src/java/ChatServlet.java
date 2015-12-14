
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ChatServlet", urlPatterns = {"/ChatServlet"})
public class ChatServlet extends HttpServlet {
    private HashMap<HttpServletResponse, HttpServletResponse> clients = new HashMap<>();
    private HashMap<String, String> salas = new HashMap<>();
    private HashMap<HttpServletResponse, Integer> tiempos = new HashMap<>();
    private HashMap<String, Integer> tiemposGrupos = new HashMap<>();
    private Lock bloqueo = new  ReentrantLock();;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/event-stream;charset=utf-8");
        clients.put(response,response);
        tiempos.put(response, 0);
        enviarSalas(request, response);
        Integer i = 0;
        while(true){
            try{
                bloqueo.lock();
                i = tiempos.get(response);
                //System.out.println(tiempos.get(response) + "  " +tiempos.get(response)+" longitud" + tiempos.size());
                if(tiempos.get(response) >= 12){
                    clients.remove(response);
                }
                tiempos.remove(response);
                tiempos.put(response, i++);
                bloqueo.unlock();
                //tiempos.put(response, tiempos.get(response)+1);
                Thread.sleep(10000);
            }catch(Exception e){

            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String text = request.getParameter("text");
        String user = request.getParameter("user");
        String tipo = request.getParameter("tipo");
        String nombreTema = request.getParameter("tema");
        bloqueo.lock();
        tiempos.remove(response);
        tiempos.put(response, 0);
        
        String idMensaje = request.getParameter("tema");
        //System.out.println(response + " = " +user + " " + tiempos.get(response) + "hay " + tiempos.size());
        if(tipo!=null){
            for(HttpServletResponse c: clients.values()){
                c.getWriter().write("event: nuevoTema\n");
                c.getWriter().write("data: {\"type\":\"message\","+"\"nombreTema\":\""+nombreTema+"\"}\n\n");
                c.getWriter().flush();
            }
            validarTiempoGrupo(nombreTema);
        }else{
            tiemposGrupos.put(nombreTema, 0);
            System.out.println("tiempo inactivo " + tiemposGrupos.get(nombreTema)+ " nombre tema 02" + nombreTema);
        }
        bloqueo.unlock();
        for(HttpServletResponse c: clients.values()){
            if(tipo==null){
                c.getWriter().write("data: {\"type\":\"message\","+"\"user\":\""+user+"\", "+"\"text\":\""+text+"\","+"\"idMensaje\":\""+idMensaje+"\"}\n\n");            
                c.getWriter().flush();
            }
        }
    }
    
    protected void enviarSalas(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        for(String c: salas.values()){
            response.getWriter().write("event: nuevoTema\n");
            response.getWriter().write("data: {\"type\":\"message\","+"\"nombreTema\":\""+c+"\"}\n\n");
            response.getWriter().flush();
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
    
    public void validarTiempoGrupo(String dato){
        salas.put(dato,dato);
        tiemposGrupos.put(dato, 0);
        while(true){
            try{
                bloqueo.lock();
                if(tiemposGrupos.get(dato) == 12){
                    salas.remove(dato);
                    System.out.println("El dato a eliminar es el siguiente "+dato);
                    for(HttpServletResponse c: clients.values()){
                        c.getWriter().write("event: quitarTema\n");
                        
                        c.getWriter().write("data: {\"type\":\"message\","+"\"nombreTema\":\""+dato+"\"}\n\n");
                        c.getWriter().flush();
                    }
                    break;
                }
                System.out.println("nombre: " + dato + " tiempo: " + tiemposGrupos.get(dato) + " longitud: "+ salas.size());
                
                tiemposGrupos.put(dato, tiemposGrupos.get(dato)+1);
                bloqueo.unlock();
                
                Thread.sleep(5000);
            }catch(Exception e){

            }
        }
    }
}
