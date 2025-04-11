import java.util.concurrent.*;

public class Barbearia {
    static int nCadeiras = 2;
    static int clientesHoje = 20;
    static Barbeiro barbeiro = new Barbeiro("Super Barbeiro Carlao");
    static ArrayBlockingQueue<Cliente> clientes = new ArrayBlockingQueue<>(nCadeiras);
    static Semaphore clienteSemafaro = new Semaphore(0);
    static Semaphore barbeiroSemafaro = new Semaphore(0);
    static Semaphore mutex = new Semaphore(1);
    static long tempoInicio = System.currentTimeMillis();

    public static void main(String[] args) {

        System.out.println("\nOBS: Como não foi informado no enunciado, estou usando random\npara gerar valores aleatorios para cada corte de cabelo, tempo até um cliente novo chegar e tempo que o barbeiro leva para acordar\n");
        System.out.println("######## A Barbearia do " + barbeiro.nome + " acabou de abrir com " + nCadeiras + " cadeiras!!! ########");

        barbeiro.start();

        for (int i = 1; i <= clientesHoje; i++) {
            new Cliente("Cliente-" + i, i).start();
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(1, 8));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static public long tempo(){
        return System.currentTimeMillis() - tempoInicio;
    }

    static class Barbeiro extends Thread {
        String nome;
        boolean dormindo;

        public Barbeiro(String nome) {
            this.nome = nome;
        }

        public void run() {
            try {
                while (true) {
                    mutex.acquire();

                    while (clientes.isEmpty()) {  
                        dormindo = true;
                        dormir();
                        mutex.release();
                        clienteSemafaro.acquire();
                        mutex.acquire();
                    }
                    Cliente cliente = clientes.poll();
                    dormindo = false;
                    mutex.release();

                    cortarCabelo(cliente.nome);

                    barbeiroSemafaro.release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void cortarCabelo(String cliente) throws InterruptedException {
            System.out.println(tempo() + " - (CORTANDO) " + nome + " está cortando o cabelo de " + cliente);
            Thread.sleep(ThreadLocalRandom.current().nextInt(2, 6));
        }

        public void dormir() throws InterruptedException {
            if(dormindo){
                System.out.println(tempo() + " - (DORMINDO) zZzZzZzZzZzZzZzZzZzZzZ");
            }else{
                Thread.sleep(ThreadLocalRandom.current().nextInt(1, 4));
                System.out.println(tempo() + " - (INDO DORMIR) zZzZzZzZzZzZzZzZzZzZzZ");
            }
        }
    }

    static class Cliente extends Thread {
        String nome;

        public Cliente(String nome, int posicao) {
            this.nome = nome;
        }

        public void run() {
            try {
                mutex.acquire();
                if (clientes.size() < nCadeiras) {
                    clientes.add(this);
                    System.out.println(tempo() + " - (ESPERANDO) O cliente " + nome + " está esperando.");
                    if (barbeiro.dormindo) {
                        acordarBarbeiro();
                    }
                    clienteSemafaro.release();
                    mutex.release();

                    barbeiroSemafaro.acquire();
                    irEmora();
                } else {
                    System.out.println(tempo() + " - (SEM ESPAÇO) O cliente " + nome + " foi embora.");
                    mutex.release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void acordarBarbeiro() throws InterruptedException {
            System.out.println(tempo() + " - (ACORDANDO) Barbeiro, acorda que eu cheguei!!!");
        }

        public void irEmora() throws InterruptedException {
            System.out.println(tempo() + " - (FINALIZADO) O cliente " + nome + " acabou de cortar o cabelo e está indo embora");
        }
    }
}
