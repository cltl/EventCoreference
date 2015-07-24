package eu.newsreader.eventcoreference.objects;/* CVS $Id: $ */
 
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/* CVS $Id: $ */

/**
 * Vocabulary definitions from sem.rdf
 * @author Auto-generated by schemagen on 10 Dec 2013 17:26
 */
public class Sem {
    /** <p>The ontology model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://semanticweb.cs.vu.nl/2009/11/sem/";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>According to is used to state which Authority sais that a property constrained
     *  by the View Constraint is true.</p>
     */
    public static final ObjectProperty accordingTo = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/accordingTo" );

    /** <p>Has actor type is used to assign a type to an actor.</p> */
    public static final ObjectProperty actorType = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/actorType" );

    /** <p>Event properties connect Events to other SEM Core entities.</p> */
    public static final ObjectProperty eventProperty = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/eventProperty" );

    /** <p>Has event type is used to assign a type to an event.</p> */
    public static final ObjectProperty eventType = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/eventType" );

    /** <p>Has actor is used to state which Actors or Objects participate in an Event.</p> */
    public static final ObjectProperty hasActor = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasActor" );

    /** <p>Has place is used to state in which Place(s) an Event happened. Coordinates
     *  can be attached to the Place with the W3C Geospatial Vocabulary (http://www.w3.org/2005/Incubator/geo/XGR-geo-20071023/).</p>
     */
    public static final ObjectProperty hasPlace = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasPlace" );

    /** <p>Has subevent connects an Event to other Events that belong to it. This is
     *  a very generic property that does not mean the subevents fully define the
     *  superconcept or that there is any causal relationship.</p>
     */
    public static final ObjectProperty hasSubEvent = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasSubEvent" );

    /** <p>Has sub type is used to state that a type falls under another type. This is
     *  a generic aggregation relation that is used to generalize over various hierarchical
     *  relations that can exist between types.</p>
     */
    public static final ObjectProperty hasSubType = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasSubType" );



    /** <p>Has time is used to indicate at which time an Event took place or when a property
     *  under the Temporary Constraint is valid. Has time points to a symbolic representation
     *  of time, which allows semantic relations between time resources. (see http://www.w3.org/TR/owl-time/)</p>
     */
    public static final ObjectProperty hasTime = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasTime" );

/*
    */
/** <p>Has BeginTime is used to indicate at which time an Event started. Has time points to a symbolic representation
     *  of time, which allows semantic relations between time resources. (see http://www.w3.org/TR/owl-time/)</p>
     *//*

    public static final ObjectProperty hasBeginTime = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasBeginTime" );

    */
/** <p>Has EndTime is used to indicate at which time an Event ended. Has time points to a symbolic representation
     *  of time, which allows semantic relations between time resources. (see http://www.w3.org/TR/owl-time/)</p>
     *//*

    public static final ObjectProperty hasEndTime = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasEndTime" );
*/

    /** <p>Has place type is used to assign a type to a place.</p> */
    public static final ObjectProperty placeType = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/placeType" );

    /** <p>Has role type is used to assign a role type to a Role property constraint.
     *  This role is a subspecification of the eventProperty which it constrains.
     *  For example, if an Event hasActor an Actor and this property is given a Role
     *  Constraint, then roleType can be used to assign a role to the participation
     *  of the Actor in the Event indicated by hasActor.</p>
     */
    public static final ObjectProperty roleType = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/roleType" );

    /** <p>Inverse property of hasSubEvent</p> */
    public static final ObjectProperty subEventOf = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/subEventOf" );

    /** <p>The inverse property of hasSubType.</p> */
    public static final ObjectProperty subTypeOf = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/subTypeOf" );

    /** <p>Has time type is used to assign a type to a time individual.</p> */
    public static final ObjectProperty timeType = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/timeType" );

    /** <p>Type is the super property of the properties that are used to indicate the
     *  type of a Core instance, eventType, actorType, placeType, timeType; and of
     *  roleType. Types can be both classes and individuals, cf. OWL 2 punning.</p>
     */
    public static final ObjectProperty type = m_model.createObjectProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/type" );

    /** <p>Has future timestamp is used to indicate the future with respect to a time instant.
     *  </p>
     */
    public static final DatatypeProperty hasFutureTimeStamp = m_model.createDatatypeProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasFutureTimeStamp" );

    /** <p>Has begin timestamp is used to indicate the beginning of a time interval.
     *  Omitting the hasBeginTimeStamp while stating a hasEndTimeStamp is interpreted
     *  as an open ended interval.</p>
     */
    public static final DatatypeProperty hasBeginTimeStamp = m_model.createDatatypeProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasBeginTimeStamp" );

    /** <p>Has earliest begin timestamp is used to indicate the earliest possible starting
     *  time of an uncertain time interval.</p>
     */
    public static final DatatypeProperty hasEarliestBeginTimeStamp = m_model.createDatatypeProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestBeginTimeStamp" );

    /** <p>Has earliest end timestamp is used to indicate the earliest possible ending
     *  time of an uncertain time interval.</p>
     */
    public static final DatatypeProperty hasEarliestEndTimeStamp = m_model.createDatatypeProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestEndTimeStamp" );

    /** <p>Has end timestamp is used to indicate the end of a time interval. Omitting
     *  the hasEndTimeStamp while stating a hasBeginTimeStamp is interpreted as an
     *  open ended interval.</p>
     */
    public static final DatatypeProperty hasEndTimeStamp = m_model.createDatatypeProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasEndTimeStamp" );

    /** <p>Has latest begin timestamp is used to indicate the latest possible starting
     *  time of an uncertain time interval.</p>
     */
    public static final DatatypeProperty hasLatestBeginTimeStamp = m_model.createDatatypeProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasLatestBeginTimeStamp" );

    /** <p>Has latest end timestamp is used to indicate the latest possible ending time
     *  of an uncertain time interval.</p>
     */
    public static final DatatypeProperty hasLatestEndTimeStamp = m_model.createDatatypeProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasLatestEndTimeStamp" );

    /** <p>Has timestamp is used to put time indicators on any individual. It is the
     *  most common way to state when an Event took place. There are subproperties
     *  of hasTimeStamp to represent time intervals and uncertain time intervals.
     *  If the exact moment is not known, but it is necessary to specify a certain
     *  time, use hasTime instead. All of these properties are also used to indicate
     *  the time at which a property under the Temporary Constraint is valid.</p>
     */
    public static final DatatypeProperty hasTimeStamp = m_model.createDatatypeProperty( "http://semanticweb.cs.vu.nl/2009/11/sem/hasTimeStamp" );

    /** <p>Actors are entities that take part in an Event, either actively or passively.
     *  Actors do not necessarily have to be sentient. They can also be objects. Actors
     *  are a thing, animate or inanimate, physical or non-physical.</p>
     */
    public static final OntClass Actor = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Actor" );

    /** <p>EventType contains all resources that are used to classify Actors, e.g. person</p> */
    public static final OntClass ActorType = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/ActorType" );

    /** <p>Authorities are entities that state SEM properties. Their nature is not specified.
     *  They can symbolize people, organizations, sources of information, etc.</p>
     */
    public static final OntClass Authority = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Authority" );

    /** <p>The SEM Constraint class contains instances of properties that have a constrained
     *  (i.e. not universal) validity. This includes time dependent validity (Temporary),
     *  validity in the guise of a specific role (Role), or validity according to
     *  a given Authority (View).</p>
     */
    public static final OntClass Constraint = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Constraint" );

    /** <p>The SEM Core class contains all entities that make up the context of an event:
     *  Events, Actors, Places, Times. This class is meant to be extended for each
     *  application domain.</p>
     */
    public static final OntClass Core = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Core" );

    /** <p>Events are things that happen. This comprises everything from historical events
     *  to web site sessions and mythical journeys. Event is the central class of
     *  SEM.</p>
     */
    public static final OntClass Event = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Event" );

    /** <p>EventType contains all resources that are used to classify Events, e.g. meeting.</p> */
    public static final OntClass EventType = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/EventType" );

    /** <p>Objects are passive, inanimate Actors.</p> */
    public static final OntClass Object = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Object" );

    /** <p>Places are locations where an Event happens. They do not need to be physical
     *  places and hence do not necessarily need coordinates. Neither do they need
     *  to have any significance apart from them being the location of an Event.</p>
     */
    public static final OntClass Place = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Place" );

    /** <p>EventType contains all resources that are used to classify Places, e.g. river.</p> */
    public static final OntClass PlaceType = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/PlaceType" );

    /** <p>Roles are properties with a subspecified function or position indicated by
     *  a RoleType in the scope of an Event. For example, the sem:hasActor property
     *  can be subspecified with the RoleType attacker, to denote that the type of
     *  participation of the Actor in the scope of the Event is "attacker". The sem:hasPlace
     *  property can be subspecified with the RoleType origin, to denote that within
     *  the scope of the Event it is the origin.</p>
     */
    public static final OntClass Role = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Role" );

    /** <p>EventType contains all resources that are used to classify Roles, e.g. receiver.</p> */
    public static final OntClass RoleType = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/RoleType" );

    /** <p>Temporaries are properties that only hold during a certain Time, which is
     *  either indicated with the sem:hasTime property to an instance of sem:Time,
     *  or with the sem:hasTimeStamp property to some timestamp(s).</p>
     */
    public static final OntClass Temporary = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Temporary" );

    /** <p>Time contains symbolic representations of when an Event took place. Time instances
     *  do not necessarily need to have a timestamp associated with them. The Time
     *  class exists to represent semantics of time, e.g. that one thing happens before
     *  another, even though the exact timestamps are unknown.</p>
     */
    public static final OntClass Time = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Time" );

    /** <p>EventType contains all resources that are used to classify Time, e.g. century.</p> */
    public static final OntClass TimeType = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/TimeType" );

    /** <p>The SEM Type class contains all types of Core instances. These can be either
     *  individuals of classes themselves. This class is meant to be extended for
     *  each application domain.</p>
     */
    public static final OntClass Type = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/Type" );

    /** <p>Views are properties that only hold according to a certain Authority.</p> */
    public static final OntClass View = m_model.createClass( "http://semanticweb.cs.vu.nl/2009/11/sem/View" );

}

