import jade.core.Agent;

import java.util.List;

public class ProfilerAgent extends Agent
{
    private int age;
    private String occupation;
    private String gender;
    private List<String> interests;
    private List<String> visited;

    protected void setup()
    {
        System.out.println("Hello World. ");
        System.out.println("My name is "+ getLocalName());
    }
}