package org.artifactory.resource;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RemoteRepoResourceTest {

    @Test
    public void testConstructorFitToReConstruct() {
        Constructor<?>[] constructors = RemoteRepoResource.class.getConstructors();
        List<Parameter> constructorParams = Arrays.asList(constructors[0].getParameters());

        Optional<Method> optionalMethod = Arrays.stream(RemoteRepoResource.class.getDeclaredMethods())
                .filter(method1 -> method1.getName().equals("reConstruct")
                        && method1.getParameterCount() > 1)
                .findFirst();
        assertTrue(optionalMethod.isPresent());
        Method reconstructMethod = optionalMethod.get();

        List<Parameter> reConstructParams = Arrays.asList(reconstructMethod.getParameters());
        assertEquals("repoPath", constructorParams.get(0).getName());
        assertEquals("Constructor fields should match Reconstruct fields", reConstructParams.size() , constructorParams.size() -1);
        IntStream.range(0, reConstructParams.size())
                .forEach(index -> {
                    assertEquals(String.format("Field number %d with name '%s' on " +
                                    "reConstruct doesn't match '%s' on Constructor."
                            ,index, reConstructParams.get(index).getName(), constructorParams.get(index + 1).getName()),
                            reConstructParams.get(index).getName(), constructorParams.get(index + 1).getName());
                    assertEquals(String.format("Field number %d with type '%s' on " +
                                    "reConstruct doesn't match '%s' on Constructor."
                            ,index, reConstructParams.get(index).getType().toString(), constructorParams.get(index + 1).getType().toString()),
                            reConstructParams.get(index).getType(), constructorParams.get(index + 1).getType());
                });
    }
}
