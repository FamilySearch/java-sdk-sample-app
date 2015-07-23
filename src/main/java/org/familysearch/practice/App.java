package org.familysearch.practice;

import org.familysearch.api.client.DiscussionState;
import org.familysearch.api.client.FamilySearchSourceDescriptionState;
import org.familysearch.api.client.PersonMatchResultsState;
import org.familysearch.api.client.PersonNonMatchesState;
import org.familysearch.api.client.ft.ChildAndParentsRelationshipState;
import org.familysearch.api.client.ft.FamilySearchFamilyTree;
import org.familysearch.api.client.ft.FamilyTreePersonState;
import org.familysearch.api.client.memories.FamilySearchMemories;
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
  private FamilySearchFamilyTree ft = null;
  private PersonState person;
  private PersonState papa;
  private PersonState mama;
  private PersonState person2;
  private String pid;
  private DiscussionState discussion;
  private MemoriesUtil imageCreator;
  private SourceDescriptionState source;
  private FamilySearchMemories fsMemories;
  private PersonState persona;

  public App (String username, String password, String developerkey) {
    this.username = username;
    this.password = password;
    this.developerKey = developerkey;
  }

  //Read the FamilySearch Family Tree
  public void readFamilyTree () {
    System.out.println("Reading FamilyTree using username, password, and developer key...");
    boolean useSandbox = true; //whether to use the sandbox reference.

    //read the Family Tree
    this.ft = new FamilySearchFamilyTree(useSandbox)
        //and authenticate.
        .authenticateViaOAuth2Password(username, password, developerKey);
  }

  //Read a Family Tree Person by Persistent ID
  //Note: Does not work on sandbox
  public void readPersonByPersistentId () {
    String username = this.username;
    String password = this.password;
    String developerKey = this.developerKey;

    String ark = "https://familysearch.org/ark:/61903/4:1:LCJ6-DVV";
    FamilyTreePersonState person = new FamilyTreePersonState(URI.create(ark))
        .authenticateViaOAuth2Password(username, password, developerKey);
    System.out.println("Reading person by persistent ID: Person " + person.getSelfUri() + " is " + person.getName().getNameForm().getFullText());
  }

  //Read a Family Person by Family Tree ID, with or without relationships
  public void readPersonByFtId (boolean withRelationships) {
    String pid = this.pid;

    FamilySearchFamilyTree ft = this.ft;

    FamilyTreePersonState person = null;
    if(withRelationships){
      person = ft.readPersonWithRelationshipsById(pid);
      System.out.println("Reading person by FamilyTreeId with relationships: Person " + pid + " is " + person.getName().getNameForm().getFullText() +
          ", who has " + person.getRelationships().size() + " relationships");
    }
    else {
      person = ft.readPersonById(pid);
      System.out.println("Reading person by FamilyTreeId without relationships: Person " + pid + " is " + person.getName().getNameForm().getFullText());
    }
  }

  //Search for Persons or Person Matches in the Family Tree
  public void searchForMatch () {
    FamilySearchFamilyTree ft = this.ft;

    //put together a search query
    GedcomxPersonSearchQueryBuilder query = new GedcomxPersonSearchQueryBuilder()
        //for a John Smith
        .name("John Smith")
            //born 1/1/1900
        .birthDate("1 January 1900")
            //son of Peter.
        .fatherName("Peter Smith");

    System.out.println("Searching for persons with name = John Smith, birthdate = 1 January 1900, and father's name = Peter Smith");

    //search the collection
    PersonSearchResultsState results = ft.searchForPersons(query);
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

  //Create Person in the Family Tree
  public void createPerson () {
    FamilySearchFamilyTree ft = this.ft;

    System.out.println("Creating person John Smith...");

    //add a person
    PersonState person = ft.addPerson(new Person()
            //named John Smith
            .name(new Name("John Smith", new NamePart(NamePartType.Given, "John"), new NamePart(NamePartType.Surname, "Smith")).preferred(true))
                //male
            .gender(GenderType.Male)
                //born in chicago in 1920
            .fact(new Fact(FactType.Birth, "1 January 1920", "Chicago, Illinois"))
                //died in new york 1980
            .fact(new Fact(FactType.Death, "1 January 1980", "New York, New York")),
        //with a change message.
        reason("Because I said so.")
    ).ifSuccessful();

    if (person.getResponse().getClientResponseStatus().getStatusCode() == 201) {
      System.out.println("\tCreation succeeded. Person can be found at " + person.getSelfUri().toString());
    }
    else {
      System.out.println("Creation failed. Response code " + person.getResponse().getClientResponseStatus().toString());
    }
    this.person2 = person;
  }

  //Create a Couple Relationship in the Family Tree
  public void createCouple () {
    FamilySearchFamilyTree ft = this.ft;

    System.out.println("Creating couple relationship...");

    PersonState husband = this.papa;
    PersonState wife = this.mama;

    RelationshipState coupleRelationship = ft.addSpouseRelationship(husband, wife, reason("Because I said so.")).ifSuccessful();
    if (coupleRelationship.getResponse().getClientResponseStatus().getStatusCode() == 201) {
      System.out.println("\tCreation succeeded. Couple relationship can be found at " + coupleRelationship.getSelfUri().toString());
    }
    else {
      System.out.println("Creation failed. Response code " + coupleRelationship.getResponse().getClientResponseStatus().toString());
    }
  }

  //Create a Child-and-Parents Relationship in the Family Tree
  public void createChildParent () {
    FamilySearchFamilyTree ft = this.ft;

    System.out.println("Creating Child-Parent relationship...");

    PersonState father = this.papa;
    PersonState mother = this.mama;
    PersonState child = this.person;

    ChildAndParentsRelationshipState childParentRelationship = ft.addChildAndParentsRelationship(child, father, mother, reason("Because I said so."));
    if (childParentRelationship.getResponse().getClientResponseStatus().getStatusCode() == 201) {
      System.out.println("\tCreation succeeded. Child-Parent relationship can be found at " + childParentRelationship.getSelfUri().toString());
    }
    else {
      System.out.println("Creation failed. Response code " + childParentRelationship.getResponse().getClientResponseStatus().toString());
    }
  }

  //Create a Source
  public void createSource () {
    FamilySearchFamilyTree ft = this.ft;

    System.out.println("Creating source...");

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
    this.source = source;
    if (source.getResponse().getClientResponseStatus().getStatusCode() == 201) {
      System.out.println("\tCreation succeeded. Source can be found at " + source.getSelfUri().toString());
    }
    else {
      System.out.println("Creation failed. Response code " + source.getResponse().getClientResponseStatus().toString());
    }
  }

  //Create a Source Reference
  public void createSourceReference () {
    System.out.println("Creating source reference...");

    //the person that will be citing the record, source, or artifact.
    PersonState person = this.person.get();

    SourceDescriptionState source = this.source;

    person.addSourceReference(source, reason("Because I said so.")).ifSuccessful(); //cite the source.

    System.out.println("\tSource reference should have been created. See reference at https://sandbox.familysearch.org/tree/#view=ancestor&person=" + person.getPerson().getId());
  }

  //Read Everything Attached to a Source
  public void readSource () {
    //the source.
    SourceDescriptionState source = this.source.get();

    SourceDescriptionState attachedReferences = ((FamilySearchSourceDescriptionState) source).queryAttachedReferences().ifSuccessful();

    //iterate through the persons attached to the source
    List<Person> persons = attachedReferences.getEntity().getPersons();
    System.out.println("Reading source at " + source.getUri() + "\n\t" + persons.size() + " person(s) attached to this source:");
    for(Person person: persons){
      System.out.println("\t" + person.getId());
    }
  }

  //Read Person for the Current User
  public void readPersonForCurrentUser () {
    FamilySearchFamilyTree ft = this.ft;

    PersonState person = ft.readPersonForCurrentUser();

    System.out.println("Reading Person for current user: Current user is " + person.getName().getNameForm().getFullText());
  }

  //Read Source References
  public void readSourceReferences () {
    //the person on which to read the source references.
    PersonState person = this.person.get();

    //load the source references for the person.
    person.loadSourceReferences();

    //read the source references.
    List<SourceReference> sourceRefs = person.getPerson().getSources();
    if (null != sourceRefs) {
      org.gedcomx.common.URI uri = sourceRefs.get(0).getDescriptionRef();
      System.out.println("Reading source references: First one found at " + uri.toString());
    }
  }

  //Read Persona References
  public void readPersonaReferences () {
    //the person on which to read the persona references.
    PersonState person = this.person.get();

    //load the persona references for the person.
    person.loadPersonaReferences();

    //read the persona references.
    List<EvidenceReference> personaRefs = person.getPerson().getEvidence();
    if (null != personaRefs) {
      org.gedcomx.common.URI uri = personaRefs.get(0).getResource();
      System.out.println("Reading persona references: First one found at " + uri.toString());
    }
  }

  //Read Discussion References
  public void readDiscussionReferences () {
    //Create discussion to be read
    DiscussionState discussion = ft.addDiscussion(new Discussion().title("Unsure of gender").details("Deets"), reason("Because I said so."));
    //Attach discussion to person
    ((FamilyTreePersonState) this.person).addDiscussionReference(discussion, reason("Because I said so."));

    //the person on which to read the discussion references.
    PersonState person = this.person.get();

    //load the discussion references for the person.
    ((FamilyTreePersonState) person).loadDiscussionReferences();

    //read the discussion references.
    List<DiscussionReference> discussionRefs = person.getPerson().findExtensionsOfType(DiscussionReference.class);
    if (null != discussionRefs) {
      org.gedcomx.common.URI uri = discussionRefs.get(0).getResource();
      System.out.println("Reading discussion references: First one found at " + uri.toString());
    }
  }

  //Read Notes
  public void readNotes () {
    //the person on which to read the notes.
    PersonState person = this.person.get();

    //create note to read
    Note note = new Note().subject("Hair color").text("Jack's hair color was puce.");
    //attach note to person
    ((FamilyTreePersonState) this.person).addNote(note);

    //load the notes for the person.
    person.loadNotes();

    //read the discussion references.
    List<Note> notes = person.getPerson().getNotes();
    if  (null != notes) {

      System.out.println("Reading notes of " + person.getName().getNameForm().getFullText() + ":");
      for(Note n: notes){
        String subject = notes.get(0).getSubject();
        String text = notes.get(0).getText();
        System.out.println("\t" + subject + ": " + text);
      }
    }
  }

  //Read Parents, Children, or Spouses
  public void readParents () {
    //the person for which to read the parents
    PersonState person = this.person.get();   //Call PersonState.get() to repull the person's info

    System.out.println("Reading parents of " + person.getName().getNameForm().getFullText() + ":");

    PersonParentsState parents = person.readParents().ifSuccessful(); //read the parents
    if (null != parents) {
      List<Person> listOfParents = parents.getPersons();
      for(Person parent: listOfParents){
        PersonState parentState = parents.readParent(parent);
        System.out.println("\t" + parentState.getName().getNameForm().getFullText());
      }
      //PersonState parentState = parents.readParent(listOfParents.get(0));
    }
  }

  public void readChildren () {
    //the person for which to read the children
    PersonState person = this.papa.get();

    System.out.println("Reading children of " + person.getName().getNameForm().getFullText() + ":");

    PersonChildrenState children = person.readChildren().ifSuccessful(); //read the children
    if (null != children) {
      List<Person> listOfChildren = children.getPersons();
      for(Person child: listOfChildren){
        PersonState childState = children.readChild(child);
        System.out.println("\t" + childState.getName().getNameForm().getFullText());
      }
    }
  }

  public void readSpouses () {
    //the person for which to read the spouses
    PersonState person = this.mama.get();

    System.out.println("Reading spouses of " + person.getName().getNameForm().getFullText() + ":");

    PersonSpousesState spouses = person.readSpouses().ifSuccessful(); //read the spouses
     if (null != spouses) {
       List<Person> listOfSpouses = spouses.getPersons();
       for(Person spouse: listOfSpouses){
         PersonState spouseState = spouses.readSpouse(spouse);
         System.out.println("\t" + spouseState.getName().getNameForm().getFullText());
       }
     }

  }

  //Read Ancestry or Descendancy
  public void readAncestry () {
    //the person for which to read the ancestry or descendancy
    PersonState person = this.person.get();

    System.out.println("Reading ancestry of " + person.getName().getNameForm().getFullText() + ":");

    AncestryResultsState state1 = person.readAncestry(); //read the ancestry
    AncestryResultsState state2 = person.readAncestry(generations(8)); //read 8 generations of the ancestry

    String ancestor1 = state1.readPerson(2).getName().getNameForm().getFullText();
    System.out.println("\tFirst ancestor to read: " + ancestor1);

    String ancestor2 = state1.getTree().getAncestor(3).getPerson().getName().getNameForm().getFullText();
    System.out.println("\tSecond ancestor to read: " + ancestor2);
  }

  public void readDescendency () {
    //the person for which to read the ancestry or descendancy
    PersonState person = this.papa.get();

    System.out.println("Reading descendency of " + person.getName().getNameForm().getFullText() + ":");

    DescendancyResultsState state1 = person.readDescendancy(); //read the descendancy
    DescendancyResultsState state2 = person.readDescendancy(generations(2)); //read 2 generations of the descendancy

    //Read a descendent's details
    String childName = state1.getTree().getRoot().getChildren().get(0).getPerson().getName().getNameForm().getFullText();
    System.out.println("\tDescendent to read: " + childName);
  }

  //Read Person Matches (i.e., Possible Duplicates)
  public void readPersonMatches () {
    //the person for which to read the matches
    PersonState person = this.person.get();

    System.out.println("Reading person matches for " + person.getName().getNameForm().getFullText() + " (" + person.getPerson().getId() + "):");

    PersonMatchResultsState matches = ((FamilyTreePersonState) person).readMatches();

    //iterate through the matches.
    List<Entry> entries = matches.getResults().getEntries();
    if (null != entries) {
      for(Entry entry: entries){
        org.gedcomx.common.URI id = entry.getId();
        System.out.println("\t" + id);
      }
    }
  }

  //Declare Not a Match
  //Note: May not work successfully in sandbox
  public void declareNotAMatch () {
    //the match results
    PersonState person = this.person.get();
    PersonMatchResultsState matches = ((FamilyTreePersonState) person).readMatches();

    //iterate through the matches.
    List<Entry> entries = matches.getResults().getEntries();

    PersonNonMatchesState state = matches.addNonMatch(entries.get(2), reason("Because I said so."));
    System.out.println("Declaring " + entries.get(2).getId() + " not a match...");
  }

  //Add a Name or Fact
  public void addName () {
    //the person to which to add the name
    PersonState person = this.person.get();

    Name name = new Name("Jake Smith", new NamePart(NamePartType.Given, "Jake"), new NamePart(NamePartType.Surname, "Smith"));
    person.addName(name.type(NameType.AlsoKnownAs), reason("Because I said so.")).ifSuccessful(); //add name

    System.out.println("Adding the name \"Jake Smith\" to " + person.getName().getNameForm().getFullText());
  }

  public void addFact () {
    //the person to which to add the fact.
    PersonState person = this.person.get();

    person.addFact(new Fact(FactType.Death, "1955", "Sweden"), reason("Because I said so.")).ifSuccessful(); //add death fact

    System.out.println("Adding a fact to " + person.getName().getNameForm().getFullText() + "\n\tView here: " + person.getUri());
  }

  //Update a Name, Gender, or Fact
  public void updateName () {
    //the person to which to update the name.
    PersonState person = this.person.get();

    Name name = person.getName();
    String originalName = name.getNameForm().getFullText();
    name.getNameForm().setFullText("Tweedle Dum");
    person.updateName(name, reason("Because I said so.")); //update name

    System.out.println("Updating name of " + originalName + " changed to " + person.get().getName().getNameForm().getFullText());
  }

  public void updateGender () {
    //the person to which to update the gender.
    PersonState person = this.person.get();

    Gender gender = person.getGender();
    String originalGender = gender.toString();
    gender.setKnownType(GenderType.Female);
    person.updateGender(gender, reason("Because I said so.")); //update gender

    System.out.println("Updating gender of " + person.getName().getNameForm().getFullText() +
        " changed from " + originalGender + " to " + person.getGender().toString());
  }

  public void updateFact () {
    //the person to which to update the fact.
    PersonState person = this.person.get();

    Fact death = person.getPerson().getFirstFactOfType(FactType.Death);
    String originalDate = death.getDate().getOriginal();
    death.setDate(new Date().original("1985"));
    person.updateFact(death, reason("Because I said so."));

    System.out.println("Updating fact of death date of " + person.getName().getNameForm().getFullText() +
        " changed from " + originalDate + " to " + person.getPerson().getFirstFactOfType(FactType.Death).getDate().getOriginal());
  }

  //Create a Discussion
  public void createDiscussion () {
    FamilySearchFamilyTree ft = this.ft;

    //add a discussion description
    DiscussionState discussion = ft.addDiscussion(new Discussion()
            //with a title.
            .title("What about this").details("Deets"),
        //with a change message.
        reason("Because I said so.")
    );
    this.discussion = discussion;
    System.out.println("Creating discussion: Find at " + discussion.getResponse().getLocation());
  }

  //Attach a Discussion
  public void attachDiscussion () {
    //the person that will be referencing the discussion.
    PersonState person = this.person.get();

    DiscussionState discussion = this.discussion;

    ((FamilyTreePersonState) person).addDiscussionReference(discussion, reason("Because I said so.")); //reference the discussion.
    System.out.println("Attaching discussion: Attached to person at https://sandbox.familysearch.org/tree/#view=ancestor&person=" + person.getPerson().getId());
  }

  //Attach a Photo to a Person
  public void attachPhotoToPerson () {
    //the person to which the photo will be attached.
    PersonState person = this.person.get();

    //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
    DataSource digitalImage = null;
    try {
      digitalImage = imageCreator.createUniqueImage("TweedleDum.jpg");
    } catch (IOException e) {
      e.printStackTrace();
    }

    //add an artifact
    SourceDescriptionState artifact = person.addArtifact(new SourceDescription()
            //with a title
            .title("Portrait of Tweedle Dum"),
        digitalImage
    );
    System.out.println("Attaching photo to person: Find at " + artifact.getSelfUri());
  }

  //Read FamilySearch Memories
  ///More complete sample: Find a specific memory
  public void readMemories () {
    boolean useSandbox = true; //whether to use the sandbox reference.
    String username = this.username;
    String password = this.password;
    String developerKey = this.developerKey;

    //read the Family Tree
    FamilySearchMemories fsMemories = new FamilySearchMemories(useSandbox)
        //and authenticate.
        .authenticateViaOAuth2Password(username, password, developerKey);
    this.fsMemories = fsMemories;
    PersonState p = fsMemories.readPerson(this.person.getUri());
  }

  //Upload Photo or Story or Document
  public void uploadArtifact () {
    FamilySearchMemories fsMemories = this.fsMemories;

    //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
    DataSource digitalImage = null;
    try {
      digitalImage = imageCreator.createUniqueImage("Obituary.jpg");
    } catch (IOException e) {
      e.printStackTrace();
    }

    //add an artifact
    SourceDescriptionState artifact = fsMemories.addArtifact(new SourceDescription()
            //with a title
            .title("Obituary for Tweedle Dum")
                //and a citation
            .citation("Generic Newspaper, 7 Aug 1955, page 1"),
        digitalImage
    );
    System.out.println("Uploading artifact: find at " + artifact.getResponse().getLocation());
  }

  //Create a Memory Persona
  public void createMemoryPersona () {
    //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
    DataSource digitalImage = null;
    try {
      digitalImage = imageCreator.createUniqueImage("TweedleDum.jpg");
    } catch (IOException e) {
      e.printStackTrace();
    }

    PersonState person = this.person.get();

    //the artifact from which a persona will be extracted.
    SourceDescriptionState artifact = ft.addArtifact(new SourceDescription()
            .title("Tweedle Dum Fishing"),
        digitalImage
    );

    //add the persona
    PersonState persona = artifact.addPersona(new Person()
      //named TweedleDum
      .name(new Name("Personaxx Tweedle Dum", new NamePart(NamePartType.Given, "Personaxx Tweedle"), new NamePart(NamePartType.Surname, "Dum")).preferred(true)));
    this.persona = persona.get();

    System.out.println("Creating memory person: find at " + persona.getResponse().getLocation());
  }

  //Create a Persona Reference
  public void createPersonaReference () {
    //the person that will be citing the record, source, or artifact.
    PersonState person = this.person;

    //the persona that was extracted from a record or artifact.
    PersonState persona = this.persona;

    //add the persona reference.
    person.addPersonaReference(persona);

    //System.out.println("Creating persona reference between " + person.getPerson().getId() + " and " + persona.getPerson().getId());
  }

  //Attach a Photo to Multiple Persons
  public void attachPhotoToMultiplePersons () {
    //the collection to which the artifact is to be added
    CollectionState fsMemories = this.fsMemories;

    //the persons to which the photo will be attached.
    PersonState person1 = this.person;
    PersonState person2 = this.papa;
    PersonState person3 = this.mama;

    //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
    DataSource digitalImage = null;
    try {
      digitalImage = imageCreator.createUniqueImage("FamilyPortrait.jpg");
    } catch (IOException e) {
      e.printStackTrace();
    }

    //add an artifact
    SourceDescriptionState artifact = fsMemories.addArtifact(new SourceDescription()
            //with a title
            .title("Family of Tweedle Dum"),
        digitalImage
    );

    person1.addMediaReference(artifact); //attach to person1
    person2.addMediaReference(artifact); //attach to person2
    person3.addMediaReference(artifact); //attach to person3

//    System.out.println("Attaching photo to multiple persons: Attaching photo at " + artifact.getResponse().getLocation() + " with" +
//        "\n\thttps://sandbox.familysearch.org/tree/#view=ancestor&person=\"" + person1.getPerson().getId() +
//        "\n\thttps://sandbox.familysearch.org/tree/#view=ancestor&person=\"" + person2.getPerson().getId() +
//        "\n\thttps://sandbox.familysearch.org/tree/#view=ancestor&person=\"" + person3.getPerson().getId());
  }

  public static void main(String[] args){
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
      app.createMemoryPersona();
      app.createPersonaReference();
      app.readPersonaReferences();
      app.attachPhotoToMultiplePersons();   //uploading but not attaching

      System.out.println("SampleApp complete." +
          "\nReady to delete example objects? (y/n)");
      if(!scan.next().equals("n")){
        app.tearDown();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Terminated.");
  }

  //Sets up objects to be used by example methods
  private void setUp () {
    System.out.println("Setting up objects for example use...");

    //Used as person and child
    this.person = ft.addPerson(new Person()
            .name(new Name("Jack Sprat", new NamePart(NamePartType.Given, "Jack"), new NamePart(NamePartType.Surname, "Sprat")).preferred(true))
            .gender(GenderType.Male)
            .fact(new Fact(FactType.Birth, "1 January 1890", "Chicago, Illinois"))
            .fact(new Fact(FactType.Death, "1 January 1970", "New York, New York")),
        reason("Because I said so.")
    ).ifSuccessful();

    this.pid = person.get().getPerson().getId();

    //Used as husband and father
    this.papa = ft.addPerson(new Person()
            .name(new Name("Papa Moose", new NamePart(NamePartType.Given, "Papa"), new NamePart(NamePartType.Surname, "Moose")).preferred(true))
            .gender(GenderType.Male)
            .fact(new Fact(FactType.Birth, "1 January 1865", "Chicago, Illinois"))
            .fact(new Fact(FactType.Death, "1 January 1945", "New York, New York")),
        reason("Because I said so.")
    ).ifSuccessful();

    //Used as wife and mother
    this.mama = ft.addPerson(new Person()
            .name(new Name("Mother Goose", new NamePart(NamePartType.Given, "Mother"), new NamePart(NamePartType.Surname, "Goose")).preferred(true))
            .gender(GenderType.Female)
            .fact(new Fact(FactType.Birth, "1 January 1865", "Chicago, Illinois"))
            .fact(new Fact(FactType.Death, "1 January 1945", "New York, New York")),
        reason("Because I said so.")
    ).ifSuccessful();

    imageCreator = new MemoriesUtil();
  }

  //Cleans up objects used by example methods
  private void tearDown () {
    System.out.println("Deleting test objects...");
    this.person.delete();
    this.papa.delete();
    this.mama.delete();
    this.person2.delete();
    this.discussion.delete();
    this.persona.delete();
    this.source.delete();
  }
}
