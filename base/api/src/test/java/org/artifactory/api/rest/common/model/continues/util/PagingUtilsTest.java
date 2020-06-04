package org.artifactory.api.rest.common.model.continues.util;

import org.apache.commons.collections.CollectionUtils;
import org.artifactory.api.rest.common.model.continues.ContinueIndexPage;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.rest.common.model.continues.FetchFunction;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Omri Ziv
 */
public class PagingUtilsTest {

    @Mock
    ArtifactoryHome artifactoryHome;
    @Mock
    ArtifactorySystemProperties artifactorySystemProperties;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryHome.bind(artifactoryHome);
        when(artifactoryHome.getArtifactoryProperties()).thenReturn(artifactorySystemProperties);
        when(artifactorySystemProperties.getLongProperty(any(ConstantValues.class)))
                .thenAnswer(invocationOnMock -> {
                    ConstantValues constantValue = invocationOnMock.getArgument(0);
                    return Long.parseLong(constantValue.getDefValue().trim());
                });
    }

    private List<FetchFunction<String>> functions =
            Arrays.asList(
                    new FetchFunction<>(this::firstFunc, 3L),
                    new FetchFunction<>(this::secondFunc, 3L),
                    new FetchFunction<>(this::thirdFunc, 0L),
                    new FetchFunction<>(this::forthFunc, 4L),
                    new FetchFunction<>(this::fifthFunc, 2L));

    private List<FetchFunction<String>> functionsWithBlanks =
            Arrays.asList(
                    new FetchFunction<>(this::firstFuncWithBlanks, 3L),
                    new FetchFunction<>(this::secondFuncWithBlanks, 3L),
                    new FetchFunction<>(this::thirdFunc, 0L),
                    new FetchFunction<>(this::forthFuncWithBlanks, 4L),
                    new FetchFunction<>(this::fifthFuncWithBlanks, 2L));

    @Test
    public void testGetPagingFromMultipleFunctions() {
        ContinueResult<String> continueResult = org.artifactory.api.rest.common.model.continues.util.PagingUtils
                .getPagingFromMultipleFunctions(0, 2, functions);
        List<String> pageableResult = continueResult.getData();

        Assert.assertEquals(2, pageableResult.size());
        Assert.assertEquals(Arrays.asList("1", "2"), pageableResult);
    }

    @Test
    public void testGetPagingFromMultipleFunctionsSecondArray() {
        ContinueResult<String> continueResult = PagingUtils.getPagingFromMultipleFunctions(3, 3, functions);
        List<String> pageableResult = continueResult.getData();

        Assert.assertEquals(3, pageableResult.size());
        Assert.assertEquals(Arrays.asList("4", "5", "6"), pageableResult);
    }

    @Test
    public void testGetPagingFromMultipleFunctionsFirstAndSecondArray() {
        ContinueResult<String> continueResult = PagingUtils.getPagingFromMultipleFunctions(0, 5, functions);
        List<String> pageableResult = continueResult.getData();

        Assert.assertEquals(5, pageableResult.size());
        Assert.assertEquals(Arrays.asList("1", "2", "3","4", "5"), pageableResult);
    }

    @Test
    public void testGetPagingFromMultipleFunctionsSecondAndThirdAndForthArray() {
        ContinueResult<String> continueResult = PagingUtils.getPagingFromMultipleFunctions(4, 4, functions);
        List<String> pageableResult = continueResult.getData();

        Assert.assertEquals(4, pageableResult.size());
        Assert.assertEquals(Arrays.asList("5", "6", "7", "8"), pageableResult);
    }

    @Test
    public void testGetPagingFromMultipleFunctionsOverflowGettingAll() {
        ContinueResult<String> continueResult = PagingUtils.getPagingFromMultipleFunctions(0, 100, functions);
        List<String> pageableResult = continueResult.getData();
        Assert.assertEquals(12, pageableResult.size());
        List<String> expected = IntStream.range(1, 13).mapToObj(Integer::toString).collect(Collectors.toList());
        Assert.assertEquals(expected, pageableResult);
    }

    @Test
    public void testGetZeroPagingFromMultipleFunctions() {
        ContinueResult<String> continueResult = PagingUtils.getPagingFromMultipleFunctions(0, 0, functions);
        List<String> pageableResult = continueResult.getData();

        Assert.assertEquals(0, pageableResult.size());
        Assert.assertEquals(Lists.newArrayList(), pageableResult);
    }

    @Test
    public void testGetPagingFromMultipleFunctionsContinuePageObj() {
        ContinueIndexPage continueIndex = new ContinueIndexPage();
        continueIndex.setContinueIndex(0L);
        continueIndex.setLimit(2L);
        ContinueResult<String> continueResult = PagingUtils.getPagingFromMultipleFunctions(continueIndex, functions);
        List<String> pageableResult = continueResult.getData();
        Assert.assertEquals(2, pageableResult.size());
        Assert.assertEquals(Arrays.asList("1", "2"), pageableResult);
    }

    @Test
    public void testGetPagingFromMultipleFunctionsContinuePageObjDefalt() {
        ContinueIndexPage continueIndex = new ContinueIndexPage();
        continueIndex.setContinueIndex(0L);
        ContinueResult<String> continueResult = org.artifactory.api.rest.common.model.continues.util.PagingUtils
                .getPagingFromMultipleFunctions(continueIndex, functions);
        List<String> pageableResult = continueResult.getData();
        Assert.assertEquals(12, pageableResult.size());
        List<String> expected = IntStream.range(1, 13).mapToObj(Integer::toString).collect(Collectors.toList());
        Assert.assertEquals(expected, pageableResult);
    }

    @Test
    public void testGetPagingFromMultipleFunctionsArraysWithBlanks() {
        ContinueResult<String> continueResult = PagingUtils.getPagingFromMultipleFunctions(4, 4, functionsWithBlanks);
        List<String> pageableResult = continueResult.getData();
        Assert.assertEquals(4, pageableResult.size());
        Assert.assertEquals(Arrays.asList("5", "6", "8", "10"), pageableResult);
        Assert.assertEquals("10", continueResult.getContinueState());
    }

    @Test
    public void testGetPagingFromMultipleFunctionsArraysWithBlanksFromBeginning() {
        ContinueResult<String> continueResult = PagingUtils.getPagingFromMultipleFunctions(0, 5, functionsWithBlanks);
        List<String> pageableResult = continueResult.getData();

        Assert.assertEquals(5, pageableResult.size());
        Assert.assertEquals(Arrays.asList("1", "2", "5", "6", "8"), pageableResult);
        Assert.assertEquals("8", continueResult.getContinueState());
    }

    @Test
    public void testGetPagingContinuation() {
        ContinueResult<String> continueResult = PagingUtils.getPagingFromMultipleFunctions(0, 2, functionsWithBlanks);
        List<String> pageableResult = continueResult.getData();
        continueResult = PagingUtils.getPagingFromMultipleFunctions(getNextContinueState(continueResult), 3, functionsWithBlanks);
        pageableResult.addAll(continueResult.getData());
        continueResult = PagingUtils.getPagingFromMultipleFunctions(getNextContinueState(continueResult), 6, functionsWithBlanks);
        pageableResult.addAll(continueResult.getData());
        Assert.assertEquals(7, pageableResult.size());
        Assert.assertEquals(Arrays.asList("1", "2", "5", "6", "8", "10", "11"), pageableResult);
        Assert.assertNull(continueResult.getContinueState());
    }

    private int getNextContinueState(ContinueResult<String> continueResult) {

        return Integer.parseInt(continueResult.getContinueState());
    }


    private ContinueResult<String> firstFunc(int skip, int limit) {
        List<String> original = Arrays.asList("1", "2", "3");
        return getStringContinueResult(skip, limit, original);

    }

    private ContinueResult<String> secondFunc(int skip, int limit) {
        List<String> original = Arrays.asList("4", "5", "6");
        return getStringContinueResult(skip, limit, original);

    }

    private ContinueResult<String> thirdFunc(int skip, int limit) {
        List<String> original = Lists.newArrayList();
        return getStringContinueResult(skip, limit, original);
    }

    private ContinueResult<String> forthFunc(int skip, int limit) {
        List<String> original = Arrays.asList("7", "8", "9", "10");
        return getStringContinueResult(skip, limit, original);

    }

    private ContinueResult<String> fifthFunc(int skip, int limit) {
        List<String> original = Arrays.asList("11", "12");
        return getStringContinueResult(skip, limit, original);

    }

    private ContinueResult<String> firstFuncWithBlanks(int skip, int limit) {
        List<String> original = Arrays.asList("1", "2", "X");
        return getStringContinueResult(skip, limit, original);
    }

    private ContinueResult<String> secondFuncWithBlanks(int skip, int limit) {
        List<String> original = Arrays.asList("X", "5", "6");
        return getStringContinueResult(skip, limit, original);
    }

    private ContinueResult<String> forthFuncWithBlanks(int skip, int limit) {
        List<String> original = Arrays.asList("X", "8", "X", "10");
        return getStringContinueResult(skip, limit, original);
    }

    private ContinueResult<String> fifthFuncWithBlanks(int skip, int limit) {
        List<String> original = Arrays.asList("11", "X");
        return getStringContinueResult(skip, limit, original);
    }

    private ContinueResult<String> getStringContinueResult(int skip, int limit, List<String> original) {
        List<String> list = original.stream().skip(skip).filter(this::nonX).limit(limit).collect(Collectors.toList());
        int continueState = CollectionUtils.isNotEmpty(list) ? original.indexOf(list.get(list.size() - 1)) + 1 : 0;
        return new ContinueResult<>(continueState + "", list);
    }


    private boolean nonX(String item) {
        return !"X".equals(item);
    }

}
