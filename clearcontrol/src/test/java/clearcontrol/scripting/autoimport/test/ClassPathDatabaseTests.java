package clearcontrol.scripting.autoimport.test;

import clearcontrol.scripting.autoimport.ClassPathResolver;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ClassPathDatabaseTests
{

  @Test
  public void test() throws ClassNotFoundException
  {

    List<String> lListOfFoundClasses = ClassPathResolver.getFullyQualifiedNames("String");
    assertEquals(1, lListOfFoundClasses.size());
    System.out.println(lListOfFoundClasses);

    lListOfFoundClasses = ClassPathResolver.getFullyQualifiedNames("Math");
    assertEquals(1, lListOfFoundClasses.size());
    System.out.println(lListOfFoundClasses);

    lListOfFoundClasses = ClassPathResolver.getFullyQualifiedNames("ScriptingEngine");
    assertEquals(1, lListOfFoundClasses.size());
    System.out.println(lListOfFoundClasses);

  }

}
