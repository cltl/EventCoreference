package eu.newsreader.eventcoreference.input;

import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.objects.SemObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 2/18/15.
 */
public class FrameNetReader extends DefaultHandler {

    static public ArrayList<String> supers = new ArrayList<String>();
    public HashMap<String, ArrayList<String>> subToSuperFrame = new HashMap<String, ArrayList<String>>();
    public HashMap<String, ArrayList<String>> superToSubFrame = new HashMap<String, ArrayList<String>>();
    String subFrame = "";
    String superFrame = "";
    String value = "";

    public FrameNetReader () {
        init();
    }

    static public void main (String [] args) {
        String fnPath = "/Users/piek/Desktop/NWR/timeline/vua-naf2jsontimeline_2015/resources/frRelation.xml";
        FrameNetReader frameNetReader = new FrameNetReader();
        frameNetReader.parseFile(fnPath);
        frameNetReader.flatRelations(2);
        Set keySet = frameNetReader.superToSubFrame.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<String> frames = frameNetReader.superToSubFrame.get(key);
            if (frames.size()>0)
            System.out.println(key+" = " + frames.toString());
        }
       // frameNetReader.printFrameNetTree();
       /* ArrayList<String> tops = frameNetReader.getTopsFrameNetTree();
        ArrayList<String> levelFrames = new ArrayList<String>();
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            frameNetReader.getLevelFrameNetTree(2, 0, levelFrames, top);
        }
        for (int i = 0; i < levelFrames.size(); i++) {
            String s = levelFrames.get(i);
            System.out.println("s = " + s);
        }*/
    }

    public void flatRelations(int level) {
        HashMap<String, ArrayList<String>> flatSubToSuperFrame = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> flatSuperToSubFrame = new HashMap<String, ArrayList<String>>();
        ArrayList<String> tops = getTopsFrameNetTree();
        ArrayList<String> levelFrames = new ArrayList<String>();
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            getLevelFrameNetTree(level, 0, levelFrames, top);
        }
      //  System.out.println("levelFrames.size() = " + levelFrames.size());
        for (int i = 0; i < levelFrames.size(); i++) {
            String levelFrame =  levelFrames.get(i);
            ArrayList<String> descendants = new ArrayList<String>();
            getDescendants(levelFrame, descendants);
            if (descendants.size()<100) {
                flatSuperToSubFrame.put(levelFrame, descendants);
                for (int j = 0; j < descendants.size(); j++) {
                    String d = descendants.get(j);
                    if (flatSubToSuperFrame.containsKey(d)) {
                        ArrayList<String> flatFrames = flatSubToSuperFrame.get(d);
                        flatFrames.add(levelFrame);
                        flatSubToSuperFrame.put(d, flatFrames);
                    } else {
                        ArrayList<String> flatFrames = new ArrayList<String>();
                        flatFrames.add(levelFrame);
                        flatSubToSuperFrame.put(d, flatFrames);
                    }
                }
            }
        }

      /*  System.out.println("subToSuperFrame.size():"+subToSuperFrame.size());
        System.out.println("superToSubFrame.size():"+superToSubFrame.size());
        System.out.println("flatSubToSuperFrame.size():"+flatSubToSuperFrame.size());
        System.out.println("flatSuperToSubFrame.size():"+flatSuperToSubFrame.size());*/
        subToSuperFrame = flatSubToSuperFrame;
        superToSubFrame = flatSuperToSubFrame;
    }

    public ArrayList<String> getTopsFrameNetTree () {
        ArrayList<String> tops = new ArrayList<String>();
        Set keySet = superToSubFrame.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (subToSuperFrame.containsKey(key)) {
                tops.add(key);
            }
        }
        return tops;
    }

    public void getLevelFrameNetTree (final int level, int currentLevel, ArrayList<String> frames, String frame) {
/*        ArrayList<String> descendants = new ArrayList<String>();
        getDescendants(frame, descendants);
        if (descendants.size()>100) {
            currentLevel--;
        }*/
        if (level==currentLevel) {
            if (!frames.contains(frame)) {
                frames.add(frame);
            }
        }
        else if (level>currentLevel) {
            if (superToSubFrame.containsKey(frame)) {
                ArrayList<String> children = superToSubFrame.get(frame);
                for (int i = 0; i < children.size(); i++) {
                    String child = children.get(i);
                    if (!frames.contains(child)) {
                        getLevelFrameNetTree(level, (currentLevel + 1), frames, child);
                    }
                }
            }
        }
    }

    public void getParentChain (String frame, ArrayList<String> parents) {
        if (subToSuperFrame.containsKey(frame)) {
            ArrayList<String> pFrames = subToSuperFrame.get(frame);
            for (int i = 0; i < pFrames.size(); i++) {
                String p = pFrames.get(i);
                if (!parents.contains(p)) {
                    parents.add(p);
                    getParentChain(p, parents);
                }
            }
        }
    }

    public void getDescendants (String frame, ArrayList<String> decendants) {
        if (superToSubFrame.containsKey(frame)) {
            ArrayList<String> cFrames = superToSubFrame.get(frame);
            for (int i = 0; i < cFrames.size(); i++) {
                String c = cFrames.get(i);
                if (!decendants.contains(c)) {
                    decendants.add(c);
                    getDescendants(c, decendants);
                }
            }
        }
    }

    public void printFrameNetTree () {
        supers = new ArrayList<String>();
        ArrayList<String> tops = getTopsFrameNetTree();
       // System.out.println("tops = " + tops);
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            if (!supers.contains(top)) {
                supers.add(top);
                ArrayList<String> children = superToSubFrame.get(top);
                System.out.println(top + ":" + children.size());
                printTree(top, 1);
            }
        }
    }

    public void printTree (String key, int level) {
        String indent = "";
        for (int i = 0; i < level; i++) {
             indent +="-";

        }
        if (superToSubFrame.containsKey(key)) {
            ArrayList<String> children = superToSubFrame.get(key);
            for (int i = 0; i < children.size(); i++) {
                String child = children.get(i);
                if (!supers.contains(child)) {
                    supers.add(child);
                    System.out.println(indent + ":" + child);
                    printTree(child, level + 1);
                }
            }
        }
    }

    public boolean frameNetConnected (SemObject event1, SemObject event2) {
        for (int i = 0; i < event1.getConcepts().size(); i++) {
            KafSense kafSense1 = event1.getConcepts().get(i);
            if (kafSense1.getResource().equalsIgnoreCase("framenet")) {
                for (int j = 0; j < event2.getConcepts().size(); j++) {
                    KafSense kafSense2 =  event2.getConcepts().get(j);
                    if (kafSense2.getResource().equalsIgnoreCase("framenet")) {
                        if (kafSense1.getSensecode().equals(kafSense2.getSensecode())) {
                            return true;
                        }
                        else {
                            if (subToSuperFrame.containsKey(kafSense1.getSensecode())) {
                                ArrayList<String> superFrames = subToSuperFrame.get(kafSense1.getSensecode());
                                if (superFrames.contains(kafSense2.getSensecode())) {
                                   /* System.out.println("event2 is super frame:"+kafSense2.getSensecode());
                                    System.out.println("superFrames.toString() = " + superFrames.toString());
                                    System.out.println("kafSense1 = " + kafSense1.getSensecode());
                                    System.out.println("kafSense2 = " + kafSense2.getSensecode());*/
                                    return true;
                                }
                                for (int k = 0; k < superFrames.size(); k++) {
                                    String frame = superFrames.get(k);
                                    if (superToSubFrame.containsKey(frame)) {
                                        ArrayList<String> subFrames = superToSubFrame.get(frame);
                                        if (subFrames.contains(kafSense2.getSensecode())) {
                                           /* System.out.println("Share super frame:"+frame);
                                            System.out.println("subFrames.toString() = " + subFrames.toString());
                                            System.out.println("kafSense1 = " + kafSense1.getSensecode());
                                            System.out.println("kafSense2 = " + kafSense2.getSensecode());*/
                                            return true;
                                        }
                                    }
                                }
                            }
                            if (superToSubFrame.containsKey(kafSense1.getSensecode())) {
                                ArrayList<String> subFrames = superToSubFrame.get(kafSense1.getSensecode());
                                if (subFrames.contains(kafSense2.getSensecode())) {
                                   /* System.out.println("event1 is super frame:"+kafSense1.getSensecode());
                                    System.out.println("subFrames.toString() = " + subFrames.toString());
                                    System.out.println("kafSense1 = " + kafSense1.getSensecode());
                                    System.out.println("kafSense2 = " + kafSense2.getSensecode());
*/
                                    return true;
                                }
                            }

                        }
                    }
                }

            }
        }
        return false;
    }


    public void parseFile(String filePath) {
    String myerror = "";
    try {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();
        InputSource inp = new InputSource (new FileReader(filePath));
        parser.parse(inp, this);
    } catch (SAXParseException err) {
        myerror = "\n** Parsing error" + ", line " + err.getLineNumber()
                + ", uri " + err.getSystemId();
        myerror += "\n" + err.getMessage();
        System.out.println("myerror = " + myerror);
    } catch (SAXException e) {
        Exception x = e;
        if (e.getException() != null)
            x = e.getException();
        myerror += "\nSAXException --" + x.getMessage();
        System.out.println("myerror = " + myerror);
    } catch (Exception eee) {
        eee.printStackTrace();
        myerror += "\nException --" + eee.getMessage();
        System.out.println("myerror = " + myerror);
    }
}//--c

    public void init () {
        subToSuperFrame = new HashMap<String, ArrayList<String>>();
        superToSubFrame = new HashMap<String, ArrayList<String>>();
        subFrame = "";
        superFrame = "";
    }

    //    <frameRelation subID="171" supID="82" subFrameName="Commerce_buy" superFrameName="Commerce_scenario" ID="360">

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("frameRelation")) {
            subFrame = "";
            superFrame = "";
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("subFrameName")) {
                    subFrame = attributes.getValue(i).trim();
                }
                else if (name.equalsIgnoreCase("superFrameName")) {
                    superFrame = attributes.getValue(i).trim();
                }
            }
            if (!subFrame.isEmpty() && !superFrame.isEmpty()) {
                if (subToSuperFrame.containsKey(subFrame)) {
                    ArrayList<String> frames = subToSuperFrame.get(subFrame);
                    frames.add(superFrame);
                    subToSuperFrame.put(subFrame, frames);
                }
                else {
                    ArrayList<String> frames = new ArrayList<String>();
                    frames.add(superFrame);
                    subToSuperFrame.put(subFrame, frames);
                }
                if (superToSubFrame.containsKey(superFrame)) {
                    ArrayList<String> frames = superToSubFrame.get(superFrame);
                    frames.add(subFrame);
                    superToSubFrame.put(superFrame, frames);
                }
                else {
                    ArrayList<String> frames = new ArrayList<String>();
                    frames.add(subFrame);
                    superToSubFrame.put(superFrame, frames);
                }
            }
        }

        value = "";
    }//--startElement

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }

    /*
Intentionally_act:51
Frugality:1
Stimulus_focus:1
Make_agreement_on_action:1
Manipulate_into_doing:1
Visiting_scenario_stay:3
Change_event_time:2
Relative_time:2
Damaging:1
Inchoative_change_of_temperature:1
Sign:1
Purpose:4
Separating:1
Measures:6
Becoming_detached:1
Being_employed:2
Substance:1
Similarity:6
Likelihood:3
Grinding:1
Setting_fire:1
Attack:4
First_rank:1
Shaped_part:2
Bearing_arms:1
Evading:2
Giving:8
Get_a_job:1
Desirability:6
Emotions_by_stimulus:6
Experience_bodily_harm:1
Processing_materials:2
Response:3
Locale:7
Receiving:1
Eventive_cognizer_affecting:3
Trial:5
Pre_transfer:3
Visitor_scenario:4
Legality:1
Process_resume:2
Locale_by_use:3
Suasion:1
Duration_scenario:3
Cause_to_make_noise:1
Motion_directional:1
Theft:1
Text_creation:2
Cause_to_start:2
Aiming:1
Manipulation:4
Relation_between_individuals:5
Cause_to_rot:1
Passing_off:1
Containing:2
Inchoative_attaching:1
Hear:1
Experiencer_focus:9
Socially_significant_history_scenario:1
Buildings:1
Sufficiency:1
Post_transfer:2
Artifact:15
Trying_out:1
Imposing_obligation:1
Grant_permission:3
Criminal_investigation:2
Cause_impact:3
Encoding:1
Social_behavior_evaluation:14
Employer's_scenario:3
Creating:2
Rising_to_a_challenge:1
Traversing:17
Arraignment:4
Subjective_influence:2
Court_examination:1
Make_cognitive_connection:1
Arithmetic_non-commutative:2
Timespan:1
Sounds:2
Transitive_action:42
Locative_relation:12
Commerce_collect:3
Departing:8
Abounding_with:3
Path_shape:5
Commerce_money-transfer:2
Delivery:1
Commerce_pay:1
Being_obligated:1
Being_born:2
Be_in_agreement_on_action:1
Attempt_obtain_food_scenario:2
Means:2
Location_in_time:1
Container_focused_placing:1
Avoiding:6
Visiting_scenario_arrival:3
Cause_to_perceive:2
Taking:1
Name_conferral:1
Communication:40
Amounting_to:1
Import_export:4
Spelling_and_pronouncing:1
Being_named:4
Employee's_scenario:3
Forgiveness:1
Aggregate:2
Posing_as:1
Emotions_of_mental_activity:2
Taking_sides:2
Assemble:1
Offenses:2
Commercial_transaction:2
Being_attached:1
Searching_scenario:2
Meet_specifications:1
Commerce_goods-transfer:3
Capability:7
Use_vehicle:2
Travel:1
Agriculture:3
Pre_giving:1
Being_awake:2
Food:1
Sociability:1
Compliance:4
Awareness:18
State_continue:1
Being_included:2
Pre_getting:2
Social_event:2
Becoming_separated:1
Visiting:3
Part_whole:8
Go_into_shape:1
Exemplar:1
Law:2
Commerce_buy:4
Memorization:2
Sending:2
Judgment_direct_address:1
Temperature:1
Trajector-Landmark:5
Make_noise:15
Rewards_and_punishments:4
Shooting_scenario:3
Coming_up_with:2
Cycle_of_existence_scenario:5
Birth:3
Cause_change_of_position_on_a_scale:2
Hit_or_miss:1
Sleep:1
Ruling_legally:1
Hostile_encounter:3
Cause_motion:11
Cause_change_of_strength:2
Calendric_unit:1
Set_relation:1
Objective_influence:2
Architectural_part:1
Intentionally_affect:37
Activity_resume:1
Membership:1
Ride_vehicle:3
Referring_by_name:2
Locale_by_ownership:1
Have_as_translation_equivalent:1
Cause_to_fragment:1
Intentional_traversing:1
Needing:1
Research:1
Correctness:1
Event:26
Endangering:1
Communication_response:1
Activity_pause:1
Hunting_scenario:2
Custom:2
Activity_finish:2
Fragmentation_scenario:2
Receiving_scenario:3
Arrest:2
Visitor_and_host:5
Mention:1
Political_locales:4
Process_end:2
Documents:1
Arriving:3
In:1
Motion:29
Hindering:1
Preventing:1
Activity_ongoing:6
Activity_prepare:2
Bounded_entity:3
Run_risk:6
Desiring:3
Silencing:2
Attempt_distant_interaction_scenario:3
Create_physical_artwork:1
Come_together:2
Competition:3
Reason:1
Impact:1
Foreign_or_domestic_country:1
Measurable_attributes:9
Process_completed_state:1
Progress:1
Individual_history:2
Commerce_sell:4
Attaching:2
Ingest_substance:1
Physical_entity:4
Process_start:3
Leadership:1
Observable_body_parts:8
Placing:13
Chatting:1
Confronting_problem:1
Deciding:2
Use_firearm:1
Frequency:1
Visit_host_stay:1
Cotheme:2
Discussion:7
Thwarting:2
Interrupt_process:1
Win_prize:1
Scrutinizing_for:2
Participation:1
Eventive_affecting:17
Communication_means:2
Respond_to_proposal:1
Scrutiny:8
Morality_evaluation:4
Death:3
Finish_competition:2
Becoming_aware:2
Cognitive_connection:2
Have_as_requirement:1
Control:1
Simultaneity:2
Cause_to_continue:2
Change_of_phase:1
Topic:2
Being_located:3
Process_continue:5
Questioning:1
Closure:2
Performers_and_roles:1
Lose_possession:2
Seeking:4
Personal_relationship:1
Clothing:4
Fluidic_motion:1
State_of_entity:1
Inhibit_movement:4
Shoot_projectiles:1
Position_on_a_scale:7
Being_in_effect:2
Commitment:2
Visit_host_arrival:2
Be_in_control:1
Desirable_event:1
Seeking_to_achieve:1
Inclination:2
Guilt_or_innocence:1
Memory:4
Prohibiting:1
Medical_conditions:1
Pardon:1
Duration_attribute:1
Required_event:3
Ceasing_to_be:2
Estimating:1
Storing:2
Expertise:1
Dead_or_alive:1
Corroding_caused:1
Cause_change_of_consistency:1
Apply_heat:3
Being_wet:1
Giving_scenario:3
Age:2
Working_on:1
Proximity_image_schema:1
Statement:20
Forgoing:1
Religious_belief:1
Coming_to_believe:2
Manipulate_into_shape:1
Fall_asleep:1
Forming_relationships:1
Artificiality:1
Killing:4
Cause_to_be_dry:1
Activity_paused_state:1
Change_of_state_initial_state:1
Obligation_scenario:5
Post_lose_possession:1
Accoutrements:3
Execute_plan:1
Cause_to_move_in_place:2
Pre_receiving:1
Cause_harm:5
Weapon:1
Cause_change:6
Gathering_up:2
Visit_host:1
Quitting_a_place:1
Assistance:1
Abundance:1
Getting:7
Dying:1
Perception_active:3
Hiding_objects:1
Part_piece:1
Be_in_agreement_on_assessment:2
Cause_to_amalgamate:2
Employing:1
Feigning:3
Expectation:1
Activity_stop:3
Perception_experience:3
Process_stop:3
Location_of_light:2
Surpassing:1
Waiting:1
Choosing:3
Linguistic_meaning:1
Entering_of_plea:1
Institutions:2
Prevarication:1
Self_motion:9
Existence:4
Jury_deliberation:1
Activity:12
Process_initial_state:2
People_by_vocation:2
Remembering_information:3
Success_or_failure:4
Speak_on_topic:1
Clemency:2
Cause_to_end:1
Event_in_history:1
Facial_expression:1
Part_orientational:2
Duplication:1
Hiring:1
Request:2
Post_getting:1
Emotions_by_possibility:1
Employment_end:2
Infrastructure:1
Judgment_communication:5
Exchange:2
Render_nonfunctional:1
Containment_relation_IS:2
Operating_a_system:3
Cause_proliferation_in_number:1
Be_subset_of:1
Receive_visitor_scenario:1
Fields:1
Natural_features:1
Attempt_suasion:1
Emotion_directed:2
Cause_temperature_change:2
Intentionally_create:10
Attempt:4
Judgment:6
Prominence:1
Undergo_change:2
Translating:2
Bungling:1
Coming_to_be:3
Gizmo:1
Cause_expansion:3
Invading:2
Bounded_region:1
Change_position_on_a_scale:4
Bringing:4
Becoming_a_member:1
Categorization:5
Causation:1
Organization:3
Elusive_goal:2
Releasing:1
Activity_start:2
Cogitation:5
Hair_configuration:1
Appearance:2
Activity_ready_state:1
Lodging_scenario:2
Conduct:1
Visitor_arrival:2
Destroying:1
Opinion:2
Adding_up:1
Experiencer_obj:1
Pre_lose_possession:2
Being_necessary:2
Process_uncompleted_state:1
Wholes_and_parts:1
Arithmetic_commutative:2
Change_of_leadership:2
Ingestion:2
Fame:1
Employment_continue:3
Process_pause:2
Transfer:5
Certainty:3
Cure:5
Cause_to_be_wet:1
Misdeed:2
Cause_change_of_phase:2
Posture:1
Attention:3
Cause_fluidic_motion:1
Process_stopped_state:3
Education_teaching:1
Criminal_process:5
Waking_up:1
Escaping:1
Social_interaction_evaluation:5
Appointing:1
Container_focused_removing:1
Cause_bodily_experience:2
Becoming:1
Expansion:1
Visiting_scenario_departing:2
Diversity:1
Operate_vehicle:4
Cause_to_resume:1
Change_posture:2
Exhaust_resource:1
Replacing:1
Adorning:1
Cycle_of_life_and_death:3
Notification_of_charges:1
Quantity:1
Detaining:1
Dimension:1
Resolve_problem:1
Removing:6
Text:3
Using:5
Want_suspect:1
Mental_property:2
Employment_start:3
Body_movement:3
Committing_crime:9
     */
}
