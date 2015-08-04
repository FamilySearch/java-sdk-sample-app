package org.familysearch.practice;

import org.familysearch.api.client.ft.ChildAndParentsRelationshipState;
import org.familysearch.api.client.ft.FamilySearchFamilyTree;
import org.gedcomx.conclusion.Fact;
import org.gedcomx.conclusion.Name;
import org.gedcomx.conclusion.NamePart;
import org.gedcomx.conclusion.Person;
import org.gedcomx.rs.client.*;
import org.gedcomx.types.FactType;
import org.gedcomx.types.GenderType;
import org.gedcomx.types.NamePartType;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.familysearch.api.client.util.FamilySearchOptions.reason;

/**
 * Created by tyganshelton on 6/22/2015.
 */
public class PersonStateExample {

  private String username;
  private String password;
  private String developerKey;
  private String pid;
  private FamilySearchFamilyTree ft = null;
  private List<PersonState> persons;

  public void doMain () throws GedcomxApplicationException {
    //Get person
    System.out.println("Get personState with pid " + this.pid + "...");
    PersonState personState = ft.readPersonById(pid).ifSuccessful();
    Person person = personState.getPerson();

    //Get Display Properties
    System.out.println("Reading person details via display properties...");

    //Get name via display propterties
    String personName = personState.getDisplayProperties().getName();
    System.out.println("\tName: " + personName);

    //Get gender via display propterties
    String gender = personState.getDisplayProperties().getGender();
    System.out.println("\tGender: " + gender);

    //Get birth date via display propterties
    String birthDate = personState.getDisplayProperties().getBirthDate();
    System.out.println("\tBirth date: " + birthDate);

    //Get birth place via display propterties
    String birthPlace = personState.getDisplayProperties().getBirthPlace();
    System.out.println("\tBirth place: " + birthPlace);

    //Get marriage date via display propterties
    String marriageDate = personState.getDisplayProperties().getMarriageDate();
    if (null != marriageDate) {
      System.out.println("\tMarriage date: " + marriageDate);
    }
    else {
      System.out.println("\tNo marriage date");
    }

    //Get marriage place via display propterties
    String marriagePlace = personState.getDisplayProperties().getMarriagePlace();
    if (null != marriagePlace) {
      System.out.println("\tMarriage place: " + marriagePlace);
    }
    else {
      System.out.println("\tNo marriage place");
    }

    //Get death date via display propterties
    String deathDate = personState.getDisplayProperties().getDeathDate();
    System.out.println("\tDeath date: " + deathDate);

    //Get death place via display propterties
    String deathPlace = personState.getDisplayProperties().getDeathPlace();
    System.out.println("\tDeath place: " + deathPlace);

    //Get lifespan via display propterties
    String lifeSpan = personState.getDisplayProperties().getLifespan();
    System.out.println("\tLifespan: " + lifeSpan);

    System.out.println("Reading person details without display properties...");

    //Get name
    personName = personState.getName().getNameForm().getFullText();
    System.out.println("\tName: " + personName);

    //Get gender
    gender = personState.getGender().getKnownType().name();
    System.out.println("\tGender: " + gender);

    //Get birth
    Fact birth = personState.getPerson().getFirstFactOfType(FactType.Birth);
    if (null != birth) {
      System.out.println("\tBirth date: " + birth.getDate().getOriginal() +
          "\n\tBirth place: " + birth.getPlace().getOriginal());
    }
    else {
      System.out.println("\tNo birth recorded");
    }

    //Get marriage
    Fact marriage = personState.getPerson().getFirstFactOfType(FactType.Marriage);
    if (null != marriage) {
      System.out.println("\tMarriage date: " + marriage.getDate().getOriginal() +
          "\n\tMarriage place: " + marriage.getPlace().getOriginal());
    }
    else {
      System.out.println("\tNo marriage recorded");
    }

    //Get death
    Fact death = personState.getPerson().getFirstFactOfType(FactType.Death);
    if (null != death) {
      System.out.println("\tDeath date: " + death.getDate().getOriginal() +
          "\n\tDeath place: " + death.getPlace().getOriginal());
    }
    else {
      System.out.println("\tNo death recorded");
    }

    //Get url
    System.out.println("\tURL: " + personState.getUri());

    //Get url for human-viewable page
    System.out.println("\tHuman-viewable page: https://sandbox.familysearch.org/tree/#view=ancestor&person=" +
        this.pid);

    //Get living status
    System.out.println("\tLiving: " + person.getLiving().toString());

    //Get Id
    System.out.println("\tID: " + person.getId());

    //Reading relationships
    System.out.println("Reading relationships...");

    //Get parents
    try {
      PersonParentsState state = personState.readParents().ifSuccessful();
      List<Person> parents = state.getPersons();
      if (null != parents) {
        System.out.println(personName + " has " + parents.size() + " parent(s):");
        for (Person p : parents) {
          System.out.println("\t" + p.getName().getNameForm().getFullText());
        }
      } else {
        System.out.println(personName + " has no parents");
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("Problem reading parents: returned " + e.getResponse().toString());
    }

    //Get spouses
    try {
      PersonSpousesState state = personState.readSpouses().ifSuccessful();
      List<Person> spouses = state.getPersons();
      if (null != spouses) {
        System.out.println(personName + " has " + spouses.size() + " spouse(s):");
        for (Person p : spouses) {
          System.out.println("\t" + p.getName().getNameForm().getFullText());
        }
      } else {
        System.out.println(personName + " has no spouses");
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("Problem reading spouses: returned " + e.getResponse().toString());
    }

    //Get children
    try {
      PersonChildrenState state = personState.readChildren().ifSuccessful();
      List<Person> children = state.getPersons();
      if (null != children) {
        System.out.println(personName + " has " + children.size() + " child(ren):");
        for (Person p : children) {
          System.out.println("\t" + p.getName().getNameForm().getFullText());
        }
      } else {
        System.out.println(personName + " has no children");
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("Problem reading children: returned " + e.getResponse().toString());
    }
  }


  public static void main (String[] args) {
    PersonStateExample app = new PersonStateExample();

    System.out.println("Running PersonStateExample" +
        "\nEnter username:");
    Scanner scan = new Scanner(System.in);
    String username = scan.next();
    System.out.println("Enter password:");
    String password = scan.next();
    System.out.println("Enter developer key:");
    String developerKey = scan.next();
    try {
      app.setUp(username, password, developerKey);
      app.doMain();
      System.out.println("PersonStateExample complete" +
          "\nReady to delete example persons? (y/n)");
      if (!scan.next().equals("n")) {
        app.tearDown();
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tThe following critical process failed, so PersonStateExample is terminating:");
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Terminated.");
  }

  private void setUp (String username, String password, String developerKey) throws GedcomxApplicationException {
    System.out.println("Creating persons and relationships to use as examples...");

    this.persons = new ArrayList<PersonState>();

    //Create FamilyTree with credentials
    this.ft = new FamilySearchFamilyTree(true)    //true signifies sandbox
        .authenticateViaOAuth2Password(username, password, developerKey).ifSuccessful()
        .get().ifSuccessful();

    //Create persons to use for examples
    PersonState person1 = ft.addPerson(new Person()
            //named Johnny Lingo
            .name(new Name("Johnny Lingo", new NamePart(NamePartType.Given, "Johnny"),
                new NamePart(NamePartType.Surname, "Lingo")).preferred(true))
                //male
            .gender(GenderType.Male)
                //born in Honolulu in 1931
            .fact(new Fact(FactType.Birth, "1 January 1931", "Honolulu, Hawaii"))
                //died in Ventura in 1988
            .fact(new Fact(FactType.Death, "1 January 1988", "Ventura, California")),
        //with a change message.
        reason("Because I said so.")
    ).ifSuccessful();
    persons.add(person1);

    //Get pid
    this.pid = person1.get().getPerson().getId();

    PersonState person2 = ft.addPerson(new Person()
            //named John Lingo
            .name(new Name("John Lingo", new NamePart(NamePartType.Given, "John"),
                new NamePart(NamePartType.Surname, "Lingo")).preferred(true))
                //male
            .gender(GenderType.Male)
                //born in Honolulu in 1905
            .fact(new Fact(FactType.Birth, "1 January 1905", "Honolulu, Hawaii"))
                //died in Honolulu in 1980
            .fact(new Fact(FactType.Death, "1 January 1980", "Honolulu, Hawaii")),
        //with a change message.
        reason("Because I said so.")
    ).ifSuccessful();
    persons.add(person2);

    PersonState person3 = ft.addPerson(new Person()
            //named Jeanette Leilokelani
            .name(new Name("Jeanette Leilokelani", new NamePart(NamePartType.Given, "Jeanette"),
                new NamePart(NamePartType.Surname, "Leilokelani")).preferred(true))
                //female
            .gender(GenderType.Female)
                //born in Honolulu in 1903
            .fact(new Fact(FactType.Birth, "1 January 1903", "Honolulu, Hawaii"))
                //died in Honolulu in 1982
            .fact(new Fact(FactType.Death, "1 January 1982", "Honolulu, Hawaii")),
        //with a change message.
        reason("Because I said so.")
    ).ifSuccessful();
    persons.add(person3);

    PersonState person4 = ft.addPerson(new Person()
            //named Mahana Ewalu Pipi Wahine
            .name(new Name("Mahana Ewalu Pipi Wahine", new NamePart(NamePartType.Given, "Jeanette"),
                new NamePart(NamePartType.Surname, "Ewalu Pipi Wahine")).preferred(true))
                //female
            .gender(GenderType.Female)
                //born in chicago in 1933
            .fact(new Fact(FactType.Birth, "1 January 1933", "Honolulu, Hawaii"))
                //died in Los Angeles in 1995
            .fact(new Fact(FactType.Death, "1 January 1995", "Los Angeles, California")),
        //with a change message.
        reason("Because I said so.")
    ).ifSuccessful();
    persons.add(person4);

    //Creating couple relationships
    RelationshipState coupleRelationship1 =
        ft.addSpouseRelationship(person2, person3, reason("Because I said so.")).ifSuccessful();
    RelationshipState coupleRelationship2 =
        ft.addSpouseRelationship(person1, person4, reason("Because I said so.")).ifSuccessful();

    //Creating parent-child relationships
    ChildAndParentsRelationshipState childParentRelationship =
        ft.addChildAndParentsRelationship(person1, person2, person3, reason("Because I said so."));
  }

  private void tearDown () {
    System.out.println("Deleting persons created...");
    for (PersonState personState: persons) {
      personState.delete();
    }
  }
}
