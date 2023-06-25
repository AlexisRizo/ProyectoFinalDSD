package frontEnd.auxiliares;

public class Text{
    public String name;
    public double fitness;

    public Text(String name, double fitness){
        this.name = name;
        this.fitness = fitness;
    }

    @Override
    public String toString(){
        return String.format(name+","+fitness);
    }
}