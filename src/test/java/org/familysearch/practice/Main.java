package org.familysearch.practice;

/**
 * @author Ryan Heaton
 */
public class Main {

  public static void main(String[] args) throws Exception {
    App app = new App("heatonra", "1234cispass", "PWRD-4Y6P-8DNG-LTV9-XDD2-JBVG-R9BD-NRRP");
    app.readPersonByPersistentId();
  }
}
