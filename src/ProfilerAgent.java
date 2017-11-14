import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.runtime.JSONFunctions;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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

    private List<Artifact> visited;
    private personalInfo pi;
    protected void setup()
    {
        System.out.println("Hello I am "+ getLocalName());

        pi = new personalInfo(21, "student", true, null);
        visited = new ArrayList<Artifact>();

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
        ArrayList<String> artifactIds;
        ArrayList<Artifact> artifacts;
        TourRequester(personalInfo pi){ this.pi = pi; }

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
                    cfp.setConversationId("Tour-Request");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Tour-Request"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received

                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer

                            try {
                                artifactIds = (ArrayList<String>) reply.getContentObject();
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        }
                        step = 2;
                    }
                    else {
                        block();
                    }
                    break;
                case 2:
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);

                    try {
                        request.setContentObject(artifactIds);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    request.setConversationId("Artifact-Request");
                    request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
                    myAgent.send(request);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Artifact-Request"),
                            MessageTemplate.MatchInReplyTo(request.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    ACLMessage artRep = myAgent.receive(mt);
                    if (artRep != null) {
                        // Reply received

                        if (artRep.getPerformative() == ACLMessage.INFORM) {
                            // This is an offer

                            try {
                                artifacts = (ArrayList<Artifact>) artRep.getContentObject();
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        }
                        step = 4;
                    }
                    else {
                        block();
                    }
                    break;
                case 4:
                    visited.addAll(artifacts);
                    step = 5;
                    break;
            }
        }

        @Override
        public boolean done() {
            return (step == 5);
        }
    }

}