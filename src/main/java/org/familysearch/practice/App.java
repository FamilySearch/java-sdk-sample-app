package org.familysearch.practice;

import org.familysearch.api.client.*;
import org.familysearch.api.client.ft.ChildAndParentsRelationshipState;
import org.familysearch.api.client.ft.FamilySearchFamilyTree;
import org.familysearch.api.client.ft.FamilyTreePersonState;
import org.familysearch.platform.ct.DiscussionReference;
import org.familysearch.platform.discussions.Discussion;
import org.gedcomx.atom.Entry;
import org.gedcomx.common.EvidenceReference;
import org.gedcomx.common.Note;
import org.gedcomx.conclusion.*;
import org.gedcomx.rs.client.*;
import org.gedcomx.rs.client.util.GedcomxPersonSearchQueryBuilder;
import org.gedcomx.source.SourceDescription;
import org.gedcomx.source.SourceReference;
import org.gedcomx.types.FactType;
import org.gedcomx.types.GenderType;
import org.gedcomx.types.NamePartType;
import org.gedcomx.types.NameType;

import javax.activation.DataSource;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Scanner;

import static org.familysearch.api.client.util.FamilySearchOptions.reason;
import static org.gedcomx.rs.client.options.QueryParameter.generations;

/**
 * Created by tyganshelton on 7/16/2015.
 */
public class App {

  private String username;
  private String password;
  private String developerKey;
  private FamilySearchFamilyTree ft;
  private String pid;
  private MemoriesUtil imageCreator;

  private FamilySearchMemories fsMemories;

  //Created and destroyed in the API
  private PersonState person;
  private PersonState papa;
  private PersonState mama;
  private PersonState person2;
  private RelationshipState coupleRelationship;
  private ChildAndParentsRelationshipState childParentRelationship;
  private SourceDescriptionState source1;
  private SourceDescriptionState source2;
  private DiscussionState discussion1;
  private DiscussionState discussion2;
  private SourceDescriptionState artifact1;
  private SourceDescriptionState artifact2;
  private SourceDescriptionState artifact3;
  private PersonState persona;

  public App (String username, String password, String developerkey) {
    this.username = username;
    this.password = password;
    this.developerKey = developerkey;
  }

  //Read the FamilySearch Family Tree
  public void readFamilyTree () throws GedcomxApplicationException {
    System.out.println("Reading FamilyTree using username, password, and developer key...");
    boolean useSandbox = true; //whether to use the sandbox reference.

    //read the Family Tree
    this.ft = new FamilySearchFamilyTree(useSandbox)
        //and authenticate.
        .authenticateViaOAuth2Password(username, password, developerKey).ifSuccessful();
  }

  //Read a Family Tree Person by Persistent ID
  //Note: Does not work on sandbox
  public void readPersonByPersistentId () {
    System.out.println("Reading person by persistent id:");

    String username = this.username;
    String password = this.password;
    String developerKey = this.developerKey;

    String ark = "https://familysearch.org/ark:/61903/4:1:LCJ6-DVV";

    try {
      FamilyTreePersonState person = new FamilyTreePersonState(URI.create(ark))
          .authenticateViaOAuth2Password(username, password, developerKey).ifSuccessful();
      System.out.println("Person " + person.getSelfUri() + " is " +
          person.getName().getNameForm().getFullText());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead failed");
      e.printStackTrace();
    }
  }

  //Read a Family Person by Family Tree ID, with or without relationships
  public void readPersonByFtId (boolean withRelationships) {
    System.out.println("Reading person by FamilyTreeId:");
    String pid = this.pid;
    FamilySearchFamilyTree ft = this.ft;
    FamilyTreePersonState person = null;

    try {
      if (withRelationships) {
        person = ft.readPersonWithRelationshipsById(pid).ifSuccessful();
        System.out.println("\tWith relationships: Person " + pid + " is " + person.getName().getNameForm().getFullText()
          + ", who has " + person.getRelationships().size() + " relationships");
      } else {
        person = ft.readPersonById(pid).ifSuccessful();
        System.out.println("\tWithout relationships: Person " + pid + " is " +
          person.getName().getNameForm().getFullText() +
          ". Find at https://sandbox.familysearch.org/tree/#view=ancestor&person=" + person.get().getPerson().getId());
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead failed.");
      e.printStackTrace();
    }
  }

  //Search for Persons or Person Matches in the Family Tree
  public void searchForMatch () {
    System.out.println("Searching for persons with name = John Smith, birthdate = 1 January 1900, " +
        "and father's name = Peter Smith");
    FamilySearchFamilyTree ft = this.ft;

    //put together a search query
    GedcomxPersonSearchQueryBuilder query = new GedcomxPersonSearchQueryBuilder()
        //for a John Smith
        .name("John Smith")
            //born 1/1/1900
        .birthDate("1 January 1900")
            //son of Peter.
        .fatherName("Peter Smith");

    try {
      //search the collection
      PersonSearchResultsState results = ft.searchForPersons(query).ifSuccessful();
      //iterate through the results...
      List<Entry> entries = results.getResults().getEntries();
      //read the person that was hit
      PersonState person = results.readPerson(entries.get(0));

      //search the collection for matches
      PersonMatchResultsState matches = ft.searchForPersonMatches(query);
      //iterate through the results...
      entries = results.getResults().getEntries();
      System.out.println("\tThere are " + entries.size() + " matching results");
      //read the person that was matched
      person = results.readPerson(entries.get(0));
      System.out.println("\tThe first matching result is " + person.getPerson().getId());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tSearch failed.");
      e.printStackTrace();
    }
  }

  //Create Person in the Family Tree
  public void createPerson () {
    System.out.println("Creating person John Smith:");
    FamilySearchFamilyTree ft = this.ft;

    //add a person
    try {
      PersonState person = ft.addPerson(new Person()
              //named John Smith
              .name(new Name("John Smith", new NamePart(NamePartType.Given, "John"), new NamePart(NamePartType.Surname,
                  "Smith")).preferred(true))
                  //male
              .gender(GenderType.Male)
                  //born in chicago in 1920
              .fact(new Fact(FactType.Birth, "1 January 1920", "Chicago, Illinois"))
                  //died in new york 1980
              .fact(new Fact(FactType.Death, "1 January 1980", "New York, New York")),
          //with a change message.
          reason("Because I said so.")
      ).ifSuccessful();
      System.out.println("\tCreation succeeded. Person can be viewed at " +
          "https://sandbox.familysearch.org/tree/#view=ancestor&person=" + person.get().getPerson().getId());
      this.person2 = person;
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tCreation failed.");
      e.printStackTrace();
    }
  }

  //Create a Couple Relationship in the Family Tree
  public void createCouple () {
    System.out.println("Creating couple relationship:");
    FamilySearchFamilyTree ft = this.ft;
    PersonState husband = this.papa;
    PersonState wife = this.mama;

    try {
      RelationshipState coupleRelationship = ft.addSpouseRelationship(husband, wife, reason("Because I said so."))
          .ifSuccessful();
      System.out.println("\tCreation succeeded. Couple relationship can be found at " + coupleRelationship.getSelfUri()
          .toString());
      this.coupleRelationship = coupleRelationship;
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tCreation failed.");
      e.printStackTrace();
    }
  }

  //Create a Child-and-Parents Relationship in the Family Tree
  public void createChildParent () {
    System.out.println("Creating Child-Parent relationship:");
    FamilySearchFamilyTree ft = this.ft;
    PersonState father = this.papa;
    PersonState mother = this.mama;
    PersonState child = this.person;

    try {
      ChildAndParentsRelationshipState childParentRelationship =
          ft.addChildAndParentsRelationship(child, father, mother, reason("Because I said so."));
      System.out.println("\tCreation succeeded. Child-Parent relationship can be found at " +
          childParentRelationship.getSelfUri().toString());
      this.childParentRelationship = childParentRelationship;
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tCreation failed.");
      e.printStackTrace();
    }
  }

  //Create a Source
  public void createSource () {
    System.out.println("Creating source:");
    FamilySearchFamilyTree ft = this.ft;

    try {
      //add a source description
      SourceDescriptionState source = ft.addSourceDescription(new SourceDescription()
              //about some resource.
              .about(org.gedcomx.common.URI.create("https://sandbox.familysearch.org/ark:/61903/4:1:KWHL-3TP"))
                  //with a title.
              .title("Birth Certificate for Jack Sprat")
                  //and a citation
              .citation("Citation for the birth certificate")
                  //and a note
              .note(new Note().text("Some note for the source.")),
          //with a change message.
          reason("Because I said so.")
      );
      System.out.println("\tCreation succeeded. Source can be found at " + source.getSelfUri().toString());
      this.source1 = source;
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tCreation failed.");
      e.printStackTrace();
    }
  }

  //Create a Source Reference
  public void createSourceReference () {
    System.out.println("Creating source reference:");

    //the person that will be citing the record, source, or artifact.
    PersonState person = this.person.get();

    if (null != this.source1) {
      SourceDescriptionState source = this.source1;

      try {
        person.addSourceReference(source, reason("Because I said so.")).ifSuccessful(); //cite the source.
        System.out.println("\tCreation succeeded. See reference at " +
            "https://sandbox.familysearch.org/tree/#view=ancestor&person=" + person.getPerson().getId());
        this.source2 = source;
      }
      catch (GedcomxApplicationException e) {
        System.out.println("\tCreation failed.");
        e.printStackTrace();
      }
    }
    else {
      System.out.println("\tCannot create source reference: source was never created in createSource()");
    }
  }

  //Read Everything Attached to a Source
  public void readSource () {
    System.out.println("Reading source:");

    if (null != this.source1) {
      //the source.
      SourceDescriptionState source = this.source1.get();

      try {
        SourceDescriptionState attachedReferences =
            ((FamilySearchSourceDescriptionState) source).queryAttachedReferences().ifSuccessful();

        //iterate through the persons attached to the source
        List<Person> persons = attachedReferences.getEntity().getPersons();
        System.out.println("\tRead source at " + source.getUri() + "\n\t" + persons.size() +
            " person(s) attached to this source:");
        for (Person person : persons) {
          System.out.println("\t" + person.getId());
        }
      }
      catch (GedcomxApplicationException e) {
        System.out.println("\tRead failed.");
        e.printStackTrace();
      }
    }
    else {
      System.out.println("\tCannot read source: source was never created in createSource()");
    }
  }

  //Read Person for the Current User
  public void readPersonForCurrentUser () {
    System.out.println("Reading person for current user:");
    FamilySearchFamilyTree ft = this.ft;

    try {
      PersonState person = ft.readPersonForCurrentUser().ifSuccessful();
      System.out.println("\tCurrent user is " + person.getName().getNameForm().getFullText());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead failed.");
      e.printStackTrace();
    }
  }

  //Read Source References
  public void readSourceReferences () {
    System.out.println("Reading source references:");

    //the person on which to read the source references.
    PersonState person = this.person.get();

    try {
      //load the source references for the person.
      person.loadSourceReferences().ifSuccessful();

      //read the source references.
      List<SourceReference> sourceRefs = person.getPerson().getSources();
      if (null != sourceRefs) {
        org.gedcomx.common.URI uri = sourceRefs.get(0).getDescriptionRef();
        System.out.println("First one found at " + uri.toString());
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tLoading source reference to read failed.");
      e.printStackTrace();
    }
  }

  //Read Persona References
  public void readPersonaReferences () {
    System.out.println("Reading persona references:");

    //the person on which to read the persona references.
    PersonState person = this.person.get();

    try {
      //load the persona references for the person.
      person.loadPersonaReferences().ifSuccessful();

      //read the persona references.
      List<EvidenceReference> personaRefs = person.getPerson().getEvidence();
      if (null != personaRefs) {
        org.gedcomx.common.URI uri = personaRefs.get(0).getResource();
        System.out.println("\tFirst one found at " + uri.toString());
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tLoading persona reference to read failed.");
      e.printStackTrace();
    }
  }

  //Read Discussion References
  public void readDiscussionReferences () {
    System.out.println("Reading discussion references: ");

    try {
      //Create discussion to be read
      DiscussionState discussion = ft.addDiscussion(new Discussion().title("Unsure of gender").details("Deets"),
          reason("Because I said so.")).ifSuccessful();
      this.discussion1 = discussion;
      //Attach discussion to person
      ((FamilyTreePersonState) this.person).addDiscussionReference(discussion, reason("Because I said so."));

      //the person on which to read the discussion references.
      PersonState person = this.person.get();

      //load the discussion references for the person.
      ((FamilyTreePersonState) person).loadDiscussionReferences().ifSuccessful();

      //read the discussion references.
      List<DiscussionReference> discussionRefs = person.getPerson().findExtensionsOfType(DiscussionReference.class);
      if (null != discussionRefs) {
        org.gedcomx.common.URI uri = discussionRefs.get(0).getResource();
        System.out.println("\tFirst one found at " + uri.toString());
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead discussion references failed.");
      e.printStackTrace();
    }
  }

  //Read Notes
  public void readNotes () {
    //the person on which to read the notes.
    PersonState person = this.person.get();
    System.out.println("Reading notes of " + person.getName().getNameForm().getFullText() + ":");

    try {
      //create note to read
      Note note = new Note().subject("Hair color").text("Jack's hair color was puce.");
      //attach note to person
      ((FamilyTreePersonState) this.person).addNote(note).ifSuccessful();

      //load the notes for the person.
      person.loadNotes().ifSuccessful();

      //read the discussion references.
      List<Note> notes = person.getPerson().getNotes();
      if (null != notes) {

        for (Note n : notes) {
          String subject = notes.get(0).getSubject();
          String text = notes.get(0).getText();
          System.out.println("\t" + subject + ": " + text);
        }
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead notes failed.");
      e.printStackTrace();
    }
  }

  //Read Parents, Children, or Spouses
  public void readParents () {
    //the person for which to read the parents
    PersonState person = this.person.get();   //Call PersonState.get() to repull the person's info
    System.out.println("Reading parents of " + person.getName().getNameForm().getFullText() + ":");

    try {
      PersonParentsState parents = person.readParents().ifSuccessful(); //read the parents
      if (null != parents) {
        List<Person> listOfParents = parents.getPersons();
        for (Person parent : listOfParents) {
          PersonState parentState = parents.readParent(parent);
          System.out.println("\t" + parentState.getName().getNameForm().getFullText());
        }
        //PersonState parentState = parents.readParent(listOfParents.get(0));
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead failed.");
      e.printStackTrace();
    }
  }

  public void readChildren () {
    //the person for which to read the children
    PersonState person = this.papa.get();
    System.out.println("Reading children of " + person.getName().getNameForm().getFullText() + ":");

    try {
      PersonChildrenState children = person.readChildren().ifSuccessful(); //read the children
      if (null != children) {
        List<Person> listOfChildren = children.getPersons();
        for (Person child : listOfChildren) {
          PersonState childState = children.readChild(child);
          System.out.println("\t" + childState.getName().getNameForm().getFullText());
        }
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead failed.");
      e.printStackTrace();
    }
  }

  public void readSpouses () {
    //the person for which to read the spouses
    PersonState person = this.mama.get();
    System.out.println("Reading spouses of " + person.getName().getNameForm().getFullText() + ":");

    try {
      PersonSpousesState spouses = person.readSpouses().ifSuccessful(); //read the spouses
      if (null != spouses) {
        List<Person> listOfSpouses = spouses.getPersons();
        for (Person spouse : listOfSpouses) {
          PersonState spouseState = spouses.readSpouse(spouse);
          System.out.println("\t" + spouseState.getName().getNameForm().getFullText());
        }
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead failed.");
      e.printStackTrace();
    }
  }

  //Read Ancestry or Descendancy
  public void readAncestry () {
    //the person for which to read the ancestry or descendancy
    PersonState person = this.person.get();
    System.out.println("Reading ancestry of " + person.getName().getNameForm().getFullText() + ":");

    try {
      AncestryResultsState state1 = person.readAncestry().ifSuccessful(); //read the ancestry
      //read 8 generations of the ancestry
      AncestryResultsState state2 = person.readAncestry(generations(8)).ifSuccessful();

      String ancestor1 = state1.readPerson(2).getName().getNameForm().getFullText();
      System.out.println("\tFirst ancestor to read: " + ancestor1);

      String ancestor2 = state1.getTree().getAncestor(3).getPerson().getName().getNameForm().getFullText();
      System.out.println("\tSecond ancestor to read: " + ancestor2);
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead failed.");
      e.printStackTrace();
    }
  }

  public void readDescendency () {
    //the person for which to read the ancestry or descendancy
    PersonState person = this.papa.get();
    System.out.println("Reading descendency of " + person.getName().getNameForm().getFullText() + ":");

    try {
      DescendancyResultsState state1 = person.readDescendancy(); //read the descendancy
      DescendancyResultsState state2 = person.readDescendancy(generations(2)); //read 2 generations of the descendancy

      //Read a descendent's details
      String childName =
          state1.getTree().getRoot().getChildren().get(0).getPerson().getName().getNameForm().getFullText();
      System.out.println("\tDescendent to read: " + childName);
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead failed.");
      e.printStackTrace();
    }
  }

  //Read Person Matches (i.e., Possible Duplicates)
  public void readPersonMatches () {
    //the person for which to read the matches
    PersonState person = this.person.get();
    System.out.println("Reading person matches for " + person.getName().getNameForm().getFullText() +
        " (" + person.getPerson().getId() + "):");

    try {
      PersonMatchResultsState matches = ((FamilyTreePersonState) person).readMatches().ifSuccessful();

      //iterate through the matches.
      List<Entry> entries = matches.getResults().getEntries();
      if (null != entries) {
        for (Entry entry : entries) {
          org.gedcomx.common.URI id = entry.getId();
          System.out.println("\t" + id);
        }
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tRead failed.");
      e.printStackTrace();
    }
  }

  //Declare Not a Match
  //Note: May not work successfully in sandbox
  public void declareNotAMatch () {
    System.out.println("Declaring not a match:");

    //the match results
    PersonState person = this.person.get();

    try {
      PersonMatchResultsState matches = ((FamilyTreePersonState) person).readMatches().ifSuccessful();

      //iterate through the matches.
      List<Entry> entries = matches.getResults().getEntries();

      if (0 != entries.size()) {
        Entry e = entries.get(0);

        PersonNonMatchesState state = matches.addNonMatch(entries.get(0), reason("Because I said so."));//.ifSuccessful();
        System.out.println("\tDeclared " + entries.get(0).getId() + " not a match");
      }
      else {
        System.out.println("\tNo possible duplicates exist to declare not a match");
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tDeclaration failed.");
      e.printStackTrace();
    }
  }

  //Add a Name or Fact
  public void addName () {
    System.out.println("Adding name:");

    //the person to which to add the name
    PersonState person = this.person.get();

    try {
      Name name =
          new Name("Jake Smith", new NamePart(NamePartType.Given, "Jake"), new NamePart(NamePartType.Surname, "Smith"));
      person.addName(name.type(NameType.AlsoKnownAs), reason("Because I said so.")).ifSuccessful(); //add name
      System.out.println("\tAdded the name \"Jake Smith\" to " + person.getName().getNameForm().getFullText());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tAddition failed.");
      e.printStackTrace();
    }
  }

  public void addFact () {
    System.out.println("Adding fact:");

    //the person to which to add the fact.
    PersonState person = this.person.get();

    try {
      //add death fact
      person.addFact(new Fact(FactType.Death, "1955", "Sweden"), reason("Because I said so.")).ifSuccessful();
      System.out.println("\tAdded a fact to " + person.getName().getNameForm().getFullText() + "\n\tView here: "
          + person.getUri());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tAddition failed.");
      e.printStackTrace();
    }
  }

  //Update a Name, Gender, or Fact
  public void updateName () {
    System.out.println("Updating name:");

    //the person to which to update the name.
    PersonState person = this.person.get();

    try {
      Name name = person.getName();
      String originalName = name.getNameForm().getFullText();
      name.getNameForm().setFullText("Tweedle Dum");
      person.updateName(name, reason("Because I said so.")); //update name
      System.out.println("\tUpdated name of " + originalName +
          ": changed to " + person.get().getName().getNameForm().getFullText());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tUpdate failed.");
      e.printStackTrace();
    }
  }

  public void updateGender () {
    System.out.println("Updating gender:");

    //the person to which to update the gender.
    PersonState person = this.person.get();

    try {
      Gender gender = person.getGender();
      String originalGender = gender.getKnownType().name();
      gender.setKnownType(GenderType.Female);
      person.updateGender(gender, reason("Because I said so.")); //update gender
      System.out.println("\tUpdated gender of " + person.getName().getNameForm().getFullText() +
          ": changed from " + originalGender + " to " + person.getGender().getKnownType().name());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tUpdate failed.");
      e.printStackTrace();
    }
  }

  public void updateFact () {
    System.out.println("Updating fact:");

    //the person to which to update the fact.
    PersonState person = this.person.get();

    try {
      Fact death = person.getPerson().getFirstFactOfType(FactType.Death);
      String originalDate = death.getDate().getOriginal();
      death.setDate(new Date().original("1985"));
      person.updateFact(death, reason("Because I said so."));
      System.out.println("\tUpdated death date of " + person.getName().getNameForm().getFullText() +
          ": changed from " + originalDate + " to " +
          person.getPerson().getFirstFactOfType(FactType.Death).getDate().getOriginal());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tUpdate failed.");
      e.printStackTrace();
    }
  }

  //Create a Discussion
  public void createDiscussion () {
    System.out.println("Creating discussion:");

    FamilySearchFamilyTree ft = this.ft;

    try {
      //add a discussion description
      DiscussionState discussion = ft.addDiscussion(new Discussion()
              //with a title.
              .title("What about this").details("Deets"),
          //with a change message.
          reason("Because I said so.")
      ).ifSuccessful();
      this.discussion2 = discussion;
      System.out.println("\tFind at " + discussion.getResponse().getLocation());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tCreation failed.");
      e.printStackTrace();
    }
  }

  //Attach a Discussion
  public void attachDiscussion () {
    System.out.println("Attaching discussion:");

    //the person that will be referencing the discussion.
    PersonState person = this.person.get();

    if (null != this.discussion2) {
      DiscussionState discussion = this.discussion2;

      try {
        ((FamilyTreePersonState) person).addDiscussionReference(discussion,
            reason("Because I said so.")).ifSuccessful(); //reference the discussion.
        System.out.println("\tAttached to person at https://sandbox.familysearch.org/tree/#view=ancestor&person=" +
            person.getPerson().getId());
      }
      catch (GedcomxApplicationException e) {
        System.out.println("\tAttach failed.");
        e.printStackTrace();
      }
    }
    else {
      System.out.println("\tCannot attach discussion: Discussion never created in createDiscussion()");
    }
  }

  //Attach a Photo to a Person
  public void attachPhotoToPerson () {
    System.out.println("Attaching photo to person:");

    //the person to which the photo will be attached.
    PersonState person = this.person.get();

    //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
    DataSource digitalImage = null;
    try {
      digitalImage = this.imageCreator.createUniqueImage("http://i60.tinypic.com/34xjigl.jpg");

      //add an artifact
      SourceDescriptionState artifact = person.addArtifact(new SourceDescription()
              //with a title
              .title("Portrait of Tweedle Dum"),
          digitalImage
      ).ifSuccessful();
      this.artifact1 = artifact;
      System.out.println("\tFind at " + artifact.getSelfUri());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tAttach photo to person failed");
      e.printStackTrace();
    }
    catch (IOException e) {
      System.out.println("\tFailed: image creation failed");
      e.printStackTrace();
    }
  }

  //Read FamilySearch Memories
  ///More complete sample: Find a specific memory
  public void readMemories () {
    System.out.println("Reading memories:");
    boolean useSandbox = true; //whether to use the sandbox reference.
    String username = this.username;
    String password = this.password;
    String developerKey = this.developerKey;

    try {
      //read the Family Tree
      FamilySearchMemories fsMemories = new FamilySearchMemories(useSandbox)
          //and authenticate.
          .authenticateViaOAuth2Password(username, password, developerKey).ifSuccessful();
      this.fsMemories = fsMemories.get();
      System.out.println("\tSuccesful");
    }
    catch (GedcomxApplicationException e) {
      System.out.println("Read failed");
      e.printStackTrace();
    }
  }

  //Upload Photo or Story or Document
  public void uploadArtifact () {
    System.out.println("Uploading artifact:");

    if (null != this.fsMemories) {

      FamilySearchMemories fsMemories = this.fsMemories;

      //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
      DataSource digitalImage = null;

      try {
        digitalImage = imageCreator.createUniqueImage("http://i62.tinypic.com/qsrwhx.jpg");

        //add an artifact
        SourceDescriptionState artifact = fsMemories.addArtifact(new SourceDescription()
                //with a title
                .title("Obituary for Tweedle Dum")
                    //and a citation
                .citation("Generic Newspaper, 7 Aug 1955, page 1"),
            digitalImage
        ).ifSuccessful();
        this.artifact2 = artifact;
        System.out.println("\tFind at " + artifact.getResponse().getLocation());
      }
      catch (GedcomxApplicationException e) {
        System.out.println("\tUpload artifact failed");
        e.printStackTrace();
      }
      catch (IOException e) {
        System.out.println("\tFailed: image creation failed");
        e.printStackTrace();
      }
    }
    else {
      System.out.println("\tCannot upload artifact: FamilySearchMemories not read successfully in readMemories()");
    }
  }

  //Create a Memory Persona
  public void createMemoryPersona () {
    System.out.println("Creating memory persona:");

    PersonState person = this.person.get();

    //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
    DataSource digitalImage = null;

    try {
      digitalImage = imageCreator.createUniqueImage("http://i61.tinypic.com/o09lkk.jpg");

      //the artifact from which a persona will be extracted.
      SourceDescriptionState artifact = ft.addArtifact(new SourceDescription()
              .title("Tweedle Dum Fishing"),
          digitalImage
      ).ifSuccessful();
      this.artifact3 = artifact;

      //add the persona
      PersonState persona = artifact.addPersona(new Person()
          .name(new Name("Persona Tweedle Dum", new NamePart(NamePartType.Given, "Persona Tweedle"),
              new NamePart(NamePartType.Surname, "Dum")).preferred(true))).ifSuccessful();
      this.persona = persona.get();

      System.out.println("\tFind at " + persona.get().getResponse().getLocation());
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tCreate memory persona failed");
      e.printStackTrace();
    }
    catch (IOException e) {
      System.out.println("\tFailed: image creation failed");
      e.printStackTrace();
    }
  }

  //Create a Persona Reference
  public void createPersonaReference () {
    System.out.println("Creating persona reference:");

    //the person that will be citing the record, source, or artifact.
    PersonState person = this.person.get();

    if (null != this.persona) {
      //the persona that was extracted from a record or artifact.
      PersonState persona = this.persona.get();

      try {
        //add the persona reference.
        person.addPersonaReference(persona).ifSuccessful();
        System.out.println("\tCreating persona reference between " + person.getPerson().getId() +
            " and " + persona.getPerson().getId());
      }
      catch (GedcomxApplicationException e) {
        System.out.println("\tCreate persona reference failed");
        e.printStackTrace();
      }
    }
    else {
      System.out.println("\tCannot create persona reference: persona never created in createMemoryPersona()");
    }
  }

  //Attach a Photo to Multiple Persons
  public void attachPhotoToMultiplePersons () {
    System.out.println("Attaching photo to multiple persons:");

    if (null != this.fsMemories) {
      //the collection to which the artifact is to be added
      CollectionState fsMemories = this.fsMemories;

      //the persons to which the photo will be attached.
      PersonState person1 = this.person.get();
      PersonState person2 = this.papa.get();
      PersonState person3 = this.mama.get();

      //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
      DataSource digitalImage = null;
      try {
        digitalImage = imageCreator.createUniqueImage("http://i62.tinypic.com/2vkn3mo.jpg");

        //add an artifact
        SourceDescriptionState artifact = fsMemories.addArtifact(new SourceDescription()
                //with a title
                .title("Family of Tweedle Dum"),
            digitalImage
        ).ifSuccessful();

        person1.addMediaReference(artifact).ifSuccessful(); //attach to person1
        person2.addMediaReference(artifact).ifSuccessful(); //attach to person2
        person3.addMediaReference(artifact).ifSuccessful(); //attach to person3

        System.out.println("\tAttached photo at " + artifact.getResponse().getLocation() + " with" +
            "\n\thttps://sandbox.familysearch.org/tree/#view=ancestor&person=\"" + person1.getPerson().getId() +
            "\n\thttps://sandbox.familysearch.org/tree/#view=ancestor&person=\"" + person2.getPerson().getId() +
            "\n\thttps://sandbox.familysearch.org/tree/#view=ancestor&person=\"" + person3.getPerson().getId());

      }
      catch (GedcomxApplicationException e) {
        System.out.println("\tAttach to multiple persons failed");
        e.printStackTrace();
      }
      catch (IOException e) {
        System.out.println("\tFailed: image creation failed");
        e.printStackTrace();
      }
    }
    else {
      System.out.println("\tCannot attach: FamilySearchMemories never created in readMemories()");
    }
  }

  public static void main(String[] args) {
    System.out.println("Running Sample App\n" +
        "Enter username:");
    Scanner scan = new Scanner(System.in);
    String username = scan.next();
    System.out.println("Enter password:");
    String password = scan.next();
    System.out.println("Enter developer key:");
    String developerKey = scan.next();

    App app = new App(username, password, developerKey);

    try {
      app.readFamilyTree();
      app.setUp();

      app.readPersonByFtId(false);
      app.readPersonByFtId(true);
      app.searchForMatch();
      app.createPerson();
      app.createCouple();
      app.createChildParent();
      app.createSource();
      app.createSourceReference();
      app.readSource();
      app.readPersonForCurrentUser();
      app.readSourceReferences();
      app.readDiscussionReferences();
      app.readNotes();
      app.readParents();
      app.readChildren();
      app.readSpouses();
      app.readAncestry();
      app.readDescendency();
      app.readPersonMatches();
      app.declareNotAMatch();
      app.addName();
      app.addFact();
      app.updateName();
      app.updateGender();
      app.updateFact();
      app.createDiscussion();
      app.attachDiscussion();
      app.attachPhotoToPerson();
      app.readMemories();
      app.uploadArtifact();
//      app.createMemoryPersona();
//      app.createPersonaReference();
      app.readPersonaReferences();
      app.attachPhotoToMultiplePersons();

      System.out.println("SampleApp complete." +
          "\nReady to delete example objects? (y/n)");
      if (!scan.next().equals("n")) {
        app.tearDown();
      }
    }
    catch (GedcomxApplicationException e) {
      System.out.println("\tThe following process critical to running App failed, so App is terminating:");
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Terminated.");
  }

  //Sets up objects to be used by example methods
  private void setUp () throws GedcomxApplicationException {
    System.out.println("Setting up objects for example use...");

      //Used as person and child
      this.person = ft.addPerson(new Person()
              .name(new Name("Jack Sprat", new NamePart(NamePartType.Given, "Jack"),
                  new NamePart(NamePartType.Surname, "Sprat")).preferred(true))
              .gender(GenderType.Male)
              .fact(new Fact(FactType.Birth, "1 January 1890", "Chicago, Illinois"))
              .fact(new Fact(FactType.Death, "1 January 1970", "New York, New York")),
          reason("Because I said so.")
      ).ifSuccessful();

      this.pid = person.get().getPerson().getId();

      //Used as husband and father
      this.papa = ft.addPerson(new Person()
              .name(new Name("Papa Moose", new NamePart(NamePartType.Given, "Papa"),
                  new NamePart(NamePartType.Surname, "Moose")).preferred(true))
              .gender(GenderType.Male)
              .fact(new Fact(FactType.Birth, "1 January 1865", "Chicago, Illinois"))
              .fact(new Fact(FactType.Death, "1 January 1945", "New York, New York")),
          reason("Because I said so.")
      ).ifSuccessful();

      //Used as wife and mother
      this.mama = ft.addPerson(new Person()
              .name(new Name("Mother Goose", new NamePart(NamePartType.Given, "Mother"),
                  new NamePart(NamePartType.Surname, "Goose")).preferred(true))
              .gender(GenderType.Female)
              .fact(new Fact(FactType.Birth, "1 January 1865", "Chicago, Illinois"))
              .fact(new Fact(FactType.Death, "1 January 1945", "New York, New York")),
          reason("Because I said so.")
      ).ifSuccessful();

    this.imageCreator = new MemoriesUtil();
  }

  //Cleans up objects used by example methods
  private void tearDown () {
    System.out.println("Deleting test objects...");

    if (null != this.person) {
      this.person.delete();
    }
    if (null != this.papa) {
      this.papa.delete();
    }
    if (null != this.mama) {
      this.mama.delete();
    }
    if (null != this.person2) {
      this.person2.delete().ifSuccessful();
    }
    if (null != this.coupleRelationship) {
      this.coupleRelationship.delete();
    }
    if (null != this.childParentRelationship) {
      this.childParentRelationship.delete();
    }
    if (null != this.source1) {
      this.source1.delete();
    }
    if (null != this.source2) {
      this.source2.delete();
    }
    if (null != this.discussion1) {
      this.discussion1.delete();
    }
    if (null != this.discussion2) {
      this.discussion2.delete();
    }
    if (null != this.artifact1) {
      this.artifact1.delete();
    }
    if (null != this.artifact2) {
      this.artifact2.delete();
    }
    if (null != this.artifact3) {
      this.artifact3.delete();
    }
    if (null != this.persona) {
      this.persona.delete();
    }
  }
}
