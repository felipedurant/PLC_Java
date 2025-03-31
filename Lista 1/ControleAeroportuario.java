import java.util.*;
public class ControleAeroportuario {
    static int pistasDisponiveis, qte;
    static long tempoInicio;
    static List<Aviao> avioes = new ArrayList<>();
    static PriorityQueue<Aviao> filaEspera = new PriorityQueue<>(Comparator.comparingLong(Aviao::getHorario));
    static final Object lock = new Object();

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        
        System.out.print("Digite a quantidade de avioes esperando para sair\n>>> ");
        qte = input.nextInt();
        for (int i= 0; i < qte; i++) {
            System.out.print("Digite o horario de saida em milissegundos do " + (i+1) + "o aviao\n>>> ");
            avioes.add(new Aviao(input.nextLong(), i+1, "Saida"));
        }

        System.out.print("Digite a quantidade de avioes que irao chegar\n>>> ");
        qte = input.nextInt();
        for (int i= 0; i < qte; i++) {
            System.out.print("Digite o horario esperado de chegada em milissegundos do " + (i+1) + "o aviao\n>>> ");
            avioes.add(new Aviao(input.nextLong(), i+1, "Chegada"));
            
        }

        System.out.print("Digite a quantidade de pistas disponiveis\n>>> ");
        pistasDisponiveis= input.nextInt();
        System.out.print("\n");
        input.close();   

        tempoInicio = System.currentTimeMillis();
        avioes.forEach(Thread::start);
        System.out.print("############## RELATORIO ################\n");
    }

    static class Aviao extends Thread {
        long horario;
        int numero;
        String tipo;
        int tempoOcupacao = 500;
        
        public Aviao(long horario, int numero, String tipo){
            if(tipo == "Chegada"){
                this.numero = numero + qte;
            }else{
                this.numero = numero;
            }
            this.tipo = tipo;
            this.horario = horario;
        }

        public long getHorario(){
            return horario;
        }

        public void ocuparPista() throws InterruptedException{
            synchronized(lock) {
                filaEspera.add(this);
                while ((pistasDisponiveis <= 0) || (filaEspera.peek() != this)) {
                    lock.wait();
                }
               pistasDisponiveis--;
               filaEspera.remove(this);
            }
        }

        public void desocuparPista() throws InterruptedException{
            synchronized(lock) {
               pistasDisponiveis++;
               lock.notifyAll();
            }
        }

        public void run() {
            try{
                long esperarHorario = (horario + tempoInicio) - System.currentTimeMillis();
                if(esperarHorario > 0){
                    Thread.sleep(esperarHorario);
                }

                ocuparPista();

                long horarioReal = System.currentTimeMillis() - tempoInicio;
                long delay = horarioReal - horario;
                System.out.print(	
                    "-----------------------------------------\n" +
                    "ID: "+ tipo.toUpperCase() + " AVIAO " +  + numero + "\n" +
                    "HORARIO ESPERADO: " + horario + "ms\n" +
                    "HORARIO REAL: " + horarioReal + "ms\n" +
                    "DELAY: " + delay + "ms\n"
                );

                Thread.sleep(tempoOcupacao);
                desocuparPista();
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("(THREAD) O avião de número " + numero + " foi interrompida");
            }

        }
    }
}

