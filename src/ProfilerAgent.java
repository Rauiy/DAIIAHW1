import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.runtime.JSONFunctions;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class ProfilerAgent extends Agent
{
    class personalInfo implements Serializable{
        final private int age;
        final private String occupation;
        final private boolean gender;
        final private List<String> interests;

        public personalInfo(int age, String occupation, boolean gender, List<String> interests) {
            this.age = age;
            this.occupation = occupation;
            this.gender = gender;
            this.interests = interests;
        }

        public int getAge() {
            return age;
        }

        public String getOccupation() {
            return occupation;
        }

        public boolean getGender() {
            return gender;
        }

        public List<String> getInterests() {
            return interests;
        }
    }

    private List<String> visited;
    private personalInfo pi;
    protected void setup()
    {
        System.out.println("Hello I am "+ getLocalName());

        pi = new personalInfo(21, "student", true, null);

        addBehaviour(new TickerBehaviour(this, 10000) {
            @Override
            protected void onTick() {
                addBehaviour(new TourRequester(pi));
            }
        });
    }

    public class TourRequester extends Behaviour {
        int step = 0;
        personalInfo pi;
        private MessageTemplate mt; // The template to receive replies
        TourRequester(personalInfo pi){ this.pi = pi;}

        @Override
        public void action() {
            switch (step){
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

                    try {
                        cfp.setContentObject(pi);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    cfp.setConversationId("virtual-tour");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("virtual-tour"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received

                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                        }


                    }
                    else {
                        block();
                    }
                    break;
                case 2:
                    break;

            }


        }

        @Override
        public boolean done() {
            return false;
        }
    }

}