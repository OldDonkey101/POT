package hx.mbt.fsm;

public class State extends  Entity{

    boolean visited = false;

    public State(String signature) {
        super(signature);
    }

    public boolean isVisited() {
        return visited;
    }
    public void visit(){
        this.visited = true;
    }
    public void clearVisitState(){
        this.visited = false;
    }
}
