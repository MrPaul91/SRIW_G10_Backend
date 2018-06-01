package models;

public class Person {

    public String id;
    public String name;
    public Task tasks[];

    //

    public float d; //Distancia respecto a la persona deseada.

    public Person(Task[] t, String n, String id){
        this.id = id;
        this.tasks = t;
        this.name = n;
    }

    public Person(float distancia){

        this.d = distancia;
    }

    public void setD(float dis){
        this.d = dis;
    }


    public String toString(){

        return this.name +" " + this.d;
    }

}
