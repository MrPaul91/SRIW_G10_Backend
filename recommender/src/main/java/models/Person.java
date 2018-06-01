package models;

public class Person {

    public String name;
    public Task tasks[];

    //

    public double d; //Distancia respecto a la persona deseada.

    public Person(Task[] t, String n){

        this.tasks = t;
        this.name = n;
    }

    public Person(Double distancia){

        this.d = distancia;
    }

    public void setD(double dis){
        this.d = dis;
    }


    public String toString(){

        return this.name +" " + this.d;
    }

}
