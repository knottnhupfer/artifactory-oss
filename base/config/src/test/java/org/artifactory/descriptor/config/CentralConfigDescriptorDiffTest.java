/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.descriptor.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.jaxb.JaxbHelper;
import org.jfrog.common.config.diff.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jfrog.common.ExceptionUtils.wrapException;
import static org.jfrog.common.config.diff.DiffUtils.DELIMITER;
import static org.testng.Assert.*;

/**
 * @author Noam Shemesh
 */
public class CentralConfigDescriptorDiffTest {

    private DiffFunctions diffFunctions;

    @BeforeMethod
    public void initCentralConfig() throws Exception {
        diffFunctions = (DiffFunctions) Class.forName(CentralConfigDescriptorImpl.class.getPackage().getName() + "." +
                DiffFunctions.class.getSimpleName() + "Impl").newInstance();
    }

    @Test
    public void testDiffOnPrimitiveValue() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();
        second.setFileUploadMaxSizeMb(first.getFileUploadMaxSizeMb() + 1);

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        assertEquals(diffs.getNumberOfDiffs(), 1);
        assertEquals(diffs.getDiffs().get(0).toString(),
                getDiffString("fileUploadMaxSizeMb", first.getFileUploadMaxSizeMb(), first.getFileUploadMaxSizeMb() + 1));
    }

    @Test
    public void testApplyOnPrimitiveValue() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();
        second.setFileUploadMaxSizeMb(first.getFileUploadMaxSizeMb() + 1);

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);


        CentralConfigDescriptor clone = applyDiffs(SerializationUtils.clone(first), diffs.getDiffs());

        assertEquals(clone.getFileUploadMaxSizeMb(), second.getFileUploadMaxSizeMb());
    }

    @Test
    public void testDiffOnMap() {
        DiffResult diffs = testOnMap();
        assertEquals(diffs.getNumberOfDiffs(), 1);
        assertEquals(diffs.getDiffs().get(0).toString(),
                getDiffString("distributionRepositoriesMap" + DELIMITER + "123456" + DELIMITER + DiffUtils.NEW_MARKER, null, "123456"));
    }

    @Test
    public void testApplyOnMap() {
        DiffResult diffs = testOnMap();

        CentralConfigDescriptor toTest =
                applyDiffs(CentralConfigTestUtils.initCentralConfig(), diffs.getDiffs());

        assertNotNull(toTest.getDistributionRepositoriesMap().get("123456"));
    }

    @Test
    public void testApplyDeletedInMap() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();
        second.getLocalRepositoriesMap().remove("local1");

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        CentralConfigDescriptor toTest =
                applyDiffs(CentralConfigTestUtils.initCentralConfig(), diffs.getDiffs());

        assertNull(toTest.getLocalRepositoriesMap().get("local1"));
    }

    @Test
    public void testApplyChangeInMap() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();
        second.getLocalRepositoriesMap().get("local1").setYumRootDepth(1000);

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        CentralConfigDescriptor toTest =
                applyDiffs(CentralConfigTestUtils.initCentralConfig(), diffs.getDiffs());

        assertEquals(toTest.getLocalRepositoriesMap().get("local1").getYumRootDepth(), 1000);
    }

    @Test
    public void testApplyChangeKeyInMap() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();
        second.getLocalRepositoriesMap().put("local45", second.getLocalRepositoriesMap().remove("local1"));
        second.getLocalRepositoriesMap().get("local45").setKey("local45");

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        CentralConfigDescriptor toTest =
                applyDiffs(CentralConfigTestUtils.initCentralConfig(), diffs.getDiffs());

        assertNull(toTest.getLocalRepositoriesMap().get("local1"));
        assertNotNull(toTest.getLocalRepositoriesMap().get("local45"));
    }

    @Test
    public void testApplyMapWithPeriodInKey() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();
        second.getLocalRepositoriesMap().put("local45.key.net", SerializationUtils.clone(second.getLocalRepositoriesMap().get("local1")));
        second.getLocalRepositoriesMap().get("local45.key.net").setKey("local45.key.net");

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        CentralConfigDescriptor toTest =
                applyDiffs(CentralConfigTestUtils.initCentralConfig(), diffs.getDiffs());

        assertNotNull(toTest.getLocalRepositoriesMap().get("local45.key.net"));
    }

    @Test
    public void testApplyDeletedOnList() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        second.getProxies().remove(1);

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        CentralConfigDescriptor res = applyDiffs(CentralConfigTestUtils.initCentralConfig(),
                diffs.getDiffs());

        assertEquals(res.getProxies().size(), 1);
        assertEquals(res.getProxies().get(0), second.getProxies().get(0));
    }

    @Test
    public void testApplySetAndDeleteComplexObject() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        second.setIndexer(new IndexerDescriptor());

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        CentralConfigDescriptorImpl res = applyDiffs(CentralConfigTestUtils.initCentralConfig(),
                diffs.getDiffs());

        assertNotNull(res.getIndexer());

        diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, res, first);
        res = applyDiffs(CentralConfigTestUtils.initCentralConfig(), diffs.getDiffs());

        assertNull(res.getIndexer());
    }

    @Test
    public void testApplyChangeInList() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        second.getProxies().get(1).setHost("proxy551");

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        CentralConfigDescriptor res = applyDiffs(CentralConfigTestUtils.initCentralConfig(),
                        diffs.getDiffs());

        assertEquals(res.getProxies().size(), 2);
        assertEquals(res.getProxies().get(1).getHost(), second.getProxies().get(1).getHost());
    }

    @Test
    public void testApplyChangeKeyOnList() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        second.getProxies().get(1).setKey("proxy551");

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        CentralConfigDescriptor res = applyDiffs(CentralConfigTestUtils.initCentralConfig(),
                        diffs.getDiffs());

        assertEquals(res.getProxies().size(), 2);
        assertEquals(res.getProxies().get(1), second.getProxies().get(1));
    }

    @Test
    public void testApplyChangeKeyOnListWithSpace() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        second.getProxies().get(1).setKey(" pro xy551  ");

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        CentralConfigDescriptor res = applyDiffs(CentralConfigTestUtils.initCentralConfig(),
                diffs.getDiffs());

        assertEquals(res.getProxies().size(), 2);
        assertEquals(res.getProxies().get(1), second.getProxies().get(1));
    }

    private DiffResult testOnMap() {
        DistributionRepoDescriptor distributionRepoDescriptor = new DistributionRepoDescriptor();
        distributionRepoDescriptor.setKey("123");
        DistributionRepoDescriptor repoDescriptor = distributionRepoDescriptor;
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        first.addDistributionRepository(repoDescriptor);
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();
        second.addDistributionRepository(repoDescriptor);

        // Diff:
        distributionRepoDescriptor = new DistributionRepoDescriptor();
        distributionRepoDescriptor.setKey("123456");
        second.addDistributionRepository(distributionRepoDescriptor);

        return diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
    }

    @Test
    public void testDiffOnList() {
        Pair<DiffResult, ProxyDescriptor> pair = testOnList();
        assertEquals(pair.getLeft().getNumberOfDiffs(), 1);
        assertEquals(pair.getLeft().getDiffs().get(0).toString(),
                getDiffString("proxies" + DELIMITER + "foo" + DELIMITER + DiffUtils.NEW_MARKER, null, pair.getRight()));
    }

    @Test
    public void testApplyNewOnList() {
        Pair<DiffResult, ProxyDescriptor> pair = testOnList();

        CentralConfigDescriptor res = applyDiffs(CentralConfigTestUtils.initCentralConfig(),
                        pair.getLeft().getDiffs());

        assertEquals(res.getProxies().size(), 3);
        assertEquals(res.getProxies().get(2), pair.getRight());
    }

    @Test
    public void testApplyOnOrderedList() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        ArrayList<RepoDescriptor> repos = new ArrayList<>(first.getLocalRepositoriesMap().values());

        first.getVirtualRepositoriesMap().get("virtual1").setRepositories(repos);
        second.getVirtualRepositoriesMap().get("virtual1").setRepositories(Lists.reverse(repos));

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);

        assertEquals(diffs.getNumberOfDiffs(), 1);

        CentralConfigDescriptor res = applyDiffs(CentralConfigTestUtils.initCentralConfig(), diffs.getDiffs());

        assertEquals(res.getVirtualRepositoriesMap().get("virtual1").getRepositories(), Lists.reverse(repos));
    }

    private CentralConfigDescriptorImpl applyDiffs(CentralConfigDescriptorImpl object, List<Diff<?>> diffs) {
        return DiffMerger.mergeDiffs(object, diffs.stream()
                .map(currDiff -> new DataDiff<>(currDiff.getFieldName(), currDiff.getRight()))
                .collect(Collectors.toList()));
    }
    
    private Pair<DiffResult, ProxyDescriptor> testOnList() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        ProxyDescriptor proxy = new ProxyDescriptor();
        proxy.setKey("foo");
        second.addProxy(proxy, false);

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        return Pair.of(diffs, proxy);
    }

    @Test
    public void testDeleteFromMap() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        DistributionRepoDescriptor distributionRepoDescriptor = new DistributionRepoDescriptor();
        distributionRepoDescriptor.setKey("123456");
        first.addDistributionRepository(distributionRepoDescriptor);

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        assertEquals(diffs.getNumberOfDiffs(), 1);
        assertEquals(diffs.getDiffs().get(0).toString(),
                getDiffString("distributionRepositoriesMap" + DELIMITER + "123456" + DELIMITER + DiffUtils.DELETED_MARKER, "123456", null));
    }

    @Test
    public void testDeleteFromList() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        ProxyDescriptor proxy = new ProxyDescriptor();
        proxy.setKey("foo");
        first.addProxy(proxy, false);

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        assertEquals(diffs.getNumberOfDiffs(), 1);
        assertEquals(diffs.getDiffs().get(0).toString(),
                getDiffString("proxies" + DELIMITER + "foo" + DELIMITER + DiffUtils.DELETED_MARKER, proxy, null));
    }

    @Test
    public void testDeleteObject() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        second.setDateFormat(null);

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        assertEquals(diffs.getNumberOfDiffs(), 1);
        assertEquals(diffs.getDiffs().get(0).toString(),
                getDiffString("dateFormat", first.getDateFormat(), null));
    }

    @Test
    public void testDiffNewObject() {
        CentralConfigDescriptorImpl first = CentralConfigTestUtils.initCentralConfig();
        CentralConfigDescriptorImpl second = CentralConfigTestUtils.initCentralConfig();

        first.setDateFormat(null);
        second.setDateFormat("1234");

        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, first, second);
        assertEquals(diffs.getNumberOfDiffs(), 1);
        assertEquals(diffs.getDiffs().get(0).toString(),
                getDiffString("dateFormat", null, "1234"));
    }

    @Test
    public void testZeroDiffsOnSameObject() {
        CentralConfigDescriptorImpl cc = CentralConfigTestUtils.initCentralConfig();
        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, cc, cc);
        assertEquals(diffs.getNumberOfDiffs(), 0);
    }

    @Test
    public void testZeroDiffsOnSimilarObject() {
        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class,
                CentralConfigTestUtils.initCentralConfig(), CentralConfigTestUtils.initCentralConfig());
        assertEquals(diffs.getNumberOfDiffs(), 0);
    }

    @Test
    public void testApplyZeroChanges() {
        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class,
                CentralConfigTestUtils.initCentralConfig(), CentralConfigTestUtils.initCentralConfig());
        CentralConfigDescriptorImpl res = applyDiffs(CentralConfigTestUtils.initCentralConfig(), diffs.getDiffs());

        assertEquals(diffFunctions.diffFor(CentralConfigDescriptorImpl.class, res, CentralConfigTestUtils.initCentralConfig()).getNumberOfDiffs(), 0);
    }

    @Test
    public void testGenericDiffAndApply() {
        CentralConfigDescriptorImpl toTest = CentralConfigTestUtils.getFullConfig();
        List<String> validates = new ArrayList<>();
        List<String> ignores = new ArrayList<>();
        applyOnEachField(toTest, CentralConfigDescriptorImpl.class, toTest, "", validates, ignores);
        System.out.println("\n\nFinished with " + validates.size() + " validations and " + ignores.size() + " ignores");
    }

    private static final List<String> TEST_IGNORED = Lists.newArrayList(
            "VcsConfiguration#type",
            "DistributionRepoDescriptor#handleReleases",
            "DistributionRepoDescriptor#handleSnapshots"
    );

    private <T> void applyOnEachField(CentralConfigDescriptorImpl rootObject, Class<T> clazz, T object,
            String fieldPrefix, List<String> validates, List<String> ignores) {
        if (clazz.getSimpleName().equals("Class")) {
            return;
        }

        DiffMerger.allFields(clazz).stream()
                .sorted(Comparator.comparing(Field::getName))
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .forEach(field -> {
                    String fieldFullName = clazz.getSimpleName() + "#" + field.getName();
                    System.out.print(fieldFullName + ": ");
                    if (field.isAnnotationPresent(DiffIgnore.class) ||
                            field.isAnnotationPresent(DiffKey.class) ||
                            field.isAnnotationPresent(DiffReference.class) ||
                            TEST_IGNORED.stream().anyMatch(fieldFullName::equalsIgnoreCase)) {
                        System.out.print("Ignoring. ");
                        ignores.add(fieldFullName);
                    } else if (DiffMerger.isPrimitiveOrWrapperOrAtomic(field.getType(), field)) {
                        callMethodSetterAndValidate(rootObject, clazz, object, field, fieldPrefix);
                        System.out.print("Validated. ");
                        validates.add(fieldFullName);
                    } else if (ClassUtils.isAssignable(field.getType(), Map.class)) {
                        System.out.print("Processing map...\n\t");
                        Map childObject = (Map) wrapException(() -> findGetter(clazz, field).invoke(object), IllegalStateException.class);
                        if (childObject.size() == 0) {
                            throw new IllegalStateException("Map is empty and not ignored: " + field.getName());
                        }

                        Class returnType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
                        childObject.forEach((k, v) -> applyOnEachField(rootObject,
                                returnType, v, appendPrefixAndSuffixToField(fieldPrefix, field, k.toString()), validates, ignores));
                        System.out.println();
                    } else if (ClassUtils.isAssignable(field.getType(), Collection.class)) {
                        System.out.print("Processing collection...\n\t");
                        Collection childObject = (Collection) wrapException(() -> findGetter(clazz, field).invoke(object), IllegalStateException.class, null, fieldFullName);
                        if (childObject.size() == 0) {
                            throw new IllegalStateException("Collection is empty and not ignored: " + field.getName());
                        }

                        Class returnType = DiffMerger.findParameterType(
                                ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);

                        if (ClassUtils.isAssignable(returnType, String.class)) {
                            callMethodSetterAndValidate(rootObject, clazz, object, field, fieldPrefix);
                        }

                        childObject.forEach(v -> applyOnEachField(rootObject, returnType, v,
                                appendPrefixAndSuffixToField(
                                        fieldPrefix,
                                        field,
                                        DiffMerger.invokeKeyMethod(returnType).apply(v).toString()
                                ),
                                validates, ignores));
                        System.out.println();
                    } else {
                        System.out.print("Processing complex...\n\t");
                        applyOnEachField(rootObject,
                                (Class) field.getType(),
                                wrapException(() -> findGetter(clazz, field).invoke(object), IllegalStateException.class, null, fieldFullName),
                                appendPrefixAndSuffixToField(fieldPrefix, field, null), validates, ignores);
                        System.out.println();
                    }
                });
    }

    private String appendPrefixAndSuffixToField(String fieldPrefix, Field field, String fieldSuffix) {
        return (StringUtils.isEmpty(fieldPrefix) ? "" : fieldPrefix + DELIMITER) +
                StringUtils.uncapitalize(field.getName()) +
                (StringUtils.isEmpty(fieldSuffix) ? "" : DELIMITER + fieldSuffix);
    }

    private <T> void callMethodSetterAndValidate(CentralConfigDescriptorImpl rootObject, Class<?> clazz, T object, Field field, String fieldPrefix) {
        Method setter = findSetter(clazz, field);
        Method getter = findGetter(clazz, field);
        // Dear dev: if the next line throws an NPE, make sure to pre-populate the relevant config fields
        // @ CentralConfigTestUtils#getFullConfig
        Object childObject = wrapException(() -> getter.invoke(object), IllegalStateException.class);
        Pair<Object, String> objectAndFieldSuffix = objectByTypeForPrimitive(setter, childObject);

        wrapException(() -> setter.invoke(object, objectAndFieldSuffix.getLeft()), IllegalStateException.class);
        DiffResult diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class,
                rootObject, CentralConfigTestUtils.getFullConfig());

        if (diffs.getNumberOfDiffs() < 1) { // Performance
            assertTrue(diffs.getNumberOfDiffs() >= 1, diffs.getDiffs().toString() +
                    " checking object: " + JaxbHelper.toXml(rootObject));
        }
        String fieldNameToFind = appendPrefixAndSuffixToField(fieldPrefix, field, objectAndFieldSuffix.getRight());
        assertTrue(diffs.getDiffs().stream()
                        .anyMatch(diff -> diff.getFieldName().equals(fieldNameToFind)),
                diffs.getDiffs().toString() + " searching for " + fieldNameToFind);

        applyDiffs(rootObject, diffs.getDiffs());

        diffs = diffFunctions.diffFor(CentralConfigDescriptorImpl.class, CentralConfigTestUtils.getFullConfig(), rootObject);
        assertEquals(diffs.getNumberOfDiffs(), 0);
    }


    private Pair<Object, String> objectByTypeForPrimitive(Method setter, Object childObject) {
        Class<?> aClass = ClassUtils.primitiveToWrapper(childObject.getClass());
        if (ClassUtils.isAssignable(aClass, String.class)) {
            return Pair.of(childObject + "abc", null);
        } else if (ClassUtils.isAssignable(aClass, Integer.class)) {
            return Pair.of((Integer) childObject + 10, null);
        } else if (ClassUtils.isAssignable(aClass, Long.class)) {
            return Pair.of((Long) childObject + 10, null);
        } else if (ClassUtils.isAssignable(aClass, Boolean.class)) {
            return Pair.of(!((Boolean) childObject), null);
        } else if (ClassUtils.isAssignable(aClass, File.class)) {
            return Pair.of(new File("/mylocation"), null);
        } else if (ClassUtils.isAssignable(aClass, Enum.class)) {
            Enum enObj = (Enum) childObject;
            Enum newEnum = Stream.of((Enum[]) enObj.getDeclaringClass().getEnumConstants())
                    .sorted()
                    .filter(en -> !en.name().equals(enObj.name()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Not enough enum constant. " +
                            "You might want to ignore this method manually in the test"));

            if (ClassUtils.isAssignable(setter.getParameterTypes()[0], String.class)) {
                return Pair.of(newEnum.name(), null);
            }

            return Pair.of(newEnum, null);
        } else if (ClassUtils.isAssignable(aClass, List.class)) {
            List list = (List) childObject;
            list.add(list.size() > 0 ? list.get(0) : null);
            return Pair.of(list, null); // Assuming DiffAtomic list
        } else if (ClassUtils.isAssignable(aClass, Set.class) &&
                ClassUtils.isAssignable((Class) getActualTypeArray(setter)[0], String.class)) {
            Set<String> set = (Set<String>) childObject;
            set.add("abc");
            return Pair.of(set, null);
        }

        throw new IllegalStateException("No support for " + aClass);
    }

    private Type[] getActualTypeArray(Method method) {
        return ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments();
    }

    private Method findSetter(Class<?> clazz, Field field) {
        String setterName = "set" + StringUtils.capitalize(field.getName());
        return Arrays.stream(clazz.getMethods())
                .filter(method -> method.getName().equals(setterName))
                .filter(method -> method.getParameterCount() == 1)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No setter for " + field.getName()));
    }

    private Method findGetter(Class<?> clazz, Field field) {
        String prefix = boolean.class.equals(field.getType()) ? "is" : "get";
        String getterName = prefix + StringUtils.capitalize(field.getName());
        return Arrays.stream(clazz.getMethods())
                .filter(method -> method.getName().equals(getterName))
                .filter(method -> method.getParameterCount() == 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No setter for " + field.getName()));
    }

    private String getDiffString(String fieldName, Object lhs, Object rhs) {
        return new DiffBuilder(new Object(), new Object(), ToStringStyle.DEFAULT_STYLE).append(fieldName, lhs, rhs).build().getDiffs().get(0).toString();
    }

}
