package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.artifactory.addon.Addon;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.support.SupportAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.repo.RepositoryNode;
import org.checkerframework.checker.units.qual.C;
import org.glassfish.jersey.internal.guava.Lists;
import org.jfrog.common.StreamSupportUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Omri Ziv
 */
public abstract class RootFetchStrategyTest {

    @Mock
    AuthorizationService authorizationService;
    @Mock
    RepositoryService repoService;
    @Mock
    AddonsManager addonsManager;
    @Mock
    SupportAddon supportAddon;
    @Mock
    CoreAddons coreAddons;
    @Mock
    private ArtifactoryContext context;
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
        when(artifactorySystemProperties.getProperty(any(ConstantValues.class)))
                .thenAnswer(invocationOnMock -> {
                    ConstantValues constantValue = invocationOnMock.getArgument(0);
                    if (constantValue == ConstantValues.orderTreeBrowserRepositoriesByType) {
                        return "distribution,local,remote,virtual";
                    }
                    return constantValue.getDefValue().trim();
                });
        ArtifactoryContextThreadBinder.bind(context);
        when(context.getRepositoryService())
                .thenReturn(repoService);
        when(context.getAuthorizationService())
                .thenReturn(authorizationService);
        when(context.beanForType(AddonsManager.class))
                .thenReturn(addonsManager);
        when(context.beanForType(RepositoryService.class))
                .thenReturn(repoService);
        when(addonsManager.addonByType(CoreAddons.class))
                .thenReturn(coreAddons);
        when(coreAddons.isAol())
                .thenReturn(false);
        when(addonsManager.addonByType(SupportAddon.class))
                .thenReturn(supportAddon);
        when(supportAddon.isSupportAddonEnabled())
                .thenReturn(false);
        when(authorizationService.userHasPermissionsOnRepositoryRoot(any()))
                .thenReturn(true);
        when(authorizationService.isAdmin())
                .thenReturn(true);
    }

    protected void createMocks() {
        List<LocalRepoDescriptor> mavenLocalRepoDescriptor = createLocalRepoDescriptors(RepoType.Maven, 10);
        List<LocalRepoDescriptor> dockerLocalRepoDescriptor = createLocalRepoDescriptors(RepoType.Docker, 5);
        List<LocalRepoDescriptor> npmLocalRepoDescriptor = createLocalRepoDescriptors(RepoType.Npm, 7);
        Map<RepoType, Integer> repoTypeRepoDescriptorMap = Maps.newTreeMap(new RepoType.RepoNameComparator());
        repoTypeRepoDescriptorMap.put(RepoType.Maven, mavenLocalRepoDescriptor.size());
        repoTypeRepoDescriptorMap.put(RepoType.Docker, dockerLocalRepoDescriptor.size());
        repoTypeRepoDescriptorMap.put(RepoType.Npm, npmLocalRepoDescriptor.size());
        List<LocalRepoDescriptor> localRepoDescriptor = Lists.newArrayList(Iterables.concat(mavenLocalRepoDescriptor,
                dockerLocalRepoDescriptor, npmLocalRepoDescriptor));

        Map<Character, List<String>> characterListMap =
                createCharacterListMap(Lists.newArrayList(localRepoDescriptor));

        LocalRepoDescriptor trashDescriptor = new LocalRepoDescriptor();
        trashDescriptor.setKey("trash");
        when(repoService.getRepoDescriptorByPackageType(RepoType.Maven))
                .thenReturn(Lists.newArrayList(mavenLocalRepoDescriptor));
        when(repoService.getRepoDescriptorByPackageType(RepoType.Docker))
                .thenReturn(Lists.newArrayList(dockerLocalRepoDescriptor));
        when(repoService.getRepoDescriptorByPackageType(RepoType.Npm))
                .thenReturn(Lists.newArrayList(npmLocalRepoDescriptor));
        when(repoService.getRepoDescriptorByPackageTypeCount())
                .thenReturn(repoTypeRepoDescriptorMap);
        when(repoService.getLocalRepoDescriptorsIncludingBuildInfo())
                .thenReturn(localRepoDescriptor);
        when(repoService.getVirtualRepoDescriptors())
                .thenReturn(Collections.emptyList());
        when(repoService.getDistributionRepoDescriptors())
                .thenReturn(Collections.emptyList());
        when(repoService.getRemoteRepoDescriptors())
                .thenReturn(Collections.emptyList());
        when(repoService.getAllRepoKeysByFirstCharMap())
                .thenReturn(characterListMap);
        when(repoService.repoDescriptorByKey(anyString()))
                .thenAnswer(invocationOnMock -> {
                    String repoKey = invocationOnMock.getArgument(0);
                    return StreamSupportUtils.stream(localRepoDescriptor)
                            .filter(repoDescriptor -> repoDescriptor.getKey().equals(repoKey))
                            .findFirst().orElse(null);
                });
        when(repoService.localRepoDescriptorByKey(TrashService.TRASH_KEY))
                .thenReturn(trashDescriptor);
        when(repoService.hasChildren(any()))
                .thenReturn(false);
    }

    protected void createMocksWithLocalAndVirtual() {
        List<LocalRepoDescriptor> mavenLocalRepoDescriptor = createLocalRepoDescriptors(RepoType.Maven, 10);
        List<LocalRepoDescriptor> dockerLocalRepoDescriptor = createLocalRepoDescriptors(RepoType.Docker, 5);
        List<LocalRepoDescriptor> npmLocalRepoDescriptor = createLocalRepoDescriptors(RepoType.Npm, 7);

        List<VirtualRepoDescriptor> mavenVirtualRepoDescriptor = createVirtualRepoDescriptors(RepoType.Maven, 3);
        List<VirtualRepoDescriptor> dockerVirtualRepoDescriptor = createVirtualRepoDescriptors(RepoType.Docker, 4);
        List<VirtualRepoDescriptor> npmVirtualRepoDescriptor = createVirtualRepoDescriptors(RepoType.Npm, 1);

        Map<RepoType, Integer> repoTypeRepoDescriptorMap = Maps.newTreeMap(new RepoType.RepoNameComparator());

        List<RepoDescriptor> mavenRepoDescriptor = Lists.newArrayList(
                Iterables.concat(mavenLocalRepoDescriptor, mavenVirtualRepoDescriptor));
        List<RepoDescriptor> dockerRepoDescriptor = Lists.newArrayList(Iterables.concat(dockerLocalRepoDescriptor,
                dockerVirtualRepoDescriptor));
        List<RepoDescriptor> npmRepoDescriptor = Lists.newArrayList(Iterables.concat(npmLocalRepoDescriptor, npmVirtualRepoDescriptor));
        List<LocalRepoDescriptor> localRepoDescriptor = Lists.newArrayList(Iterables.concat(mavenLocalRepoDescriptor,
                dockerLocalRepoDescriptor, npmLocalRepoDescriptor));
        List<VirtualRepoDescriptor> virtualRepoDescriptor = Lists.newArrayList(Iterables.concat(mavenVirtualRepoDescriptor,
                dockerVirtualRepoDescriptor, npmVirtualRepoDescriptor));

        List<RepoDescriptor> allRepoDescriptor = Lists.newArrayList(Iterables.concat(localRepoDescriptor, virtualRepoDescriptor));
        Map<Character, List<String>> characterListMap =
                createCharacterListMap(allRepoDescriptor);
        repoTypeRepoDescriptorMap.put(RepoType.Maven, mavenRepoDescriptor.size());
        repoTypeRepoDescriptorMap.put(RepoType.Docker, dockerRepoDescriptor.size());
        repoTypeRepoDescriptorMap.put(RepoType.Npm, npmRepoDescriptor.size());
        LocalRepoDescriptor trashDescriptor = new LocalRepoDescriptor();
        trashDescriptor.setKey("trash");


        when(repoService.getRepoDescriptorByPackageType(RepoType.Maven))
                .thenReturn(mavenRepoDescriptor);
        when(repoService.getRepoDescriptorByPackageType(RepoType.Docker))
                .thenReturn(dockerRepoDescriptor);
        when(repoService.getRepoDescriptorByPackageType(RepoType.Npm))
                .thenReturn(npmRepoDescriptor);
        when(repoService.getRepoDescriptorByPackageTypeCount())
                .thenReturn(repoTypeRepoDescriptorMap);
        when(repoService.getLocalRepoDescriptorsIncludingBuildInfo())
                .thenReturn(localRepoDescriptor);
        when(repoService.getVirtualRepoDescriptors())
                .thenReturn(virtualRepoDescriptor);
        when(repoService.getDistributionRepoDescriptors())
                .thenReturn(Collections.emptyList());
        when(repoService.getRemoteRepoDescriptors())
                .thenReturn(Collections.emptyList());
        when(repoService.getAllRepoKeysByFirstCharMap())
                .thenReturn(characterListMap);
        when(repoService.repoDescriptorByKey(anyString()))
                .thenAnswer(invocationOnMock -> {
                    String repoKey = invocationOnMock.getArgument(0);
                    return StreamSupportUtils.stream(allRepoDescriptor)
                            .filter(repoDescriptor -> repoDescriptor.getKey().equals(repoKey))
                            .findFirst().orElse(null);
                });
        when(repoService.localRepoDescriptorByKey(TrashService.TRASH_KEY))
                .thenReturn(trashDescriptor);
        when(repoService.hasChildren(any()))
                .thenReturn(false);
    }

    protected boolean testRepositoryNodeBefore(List<? extends RestModel> repositoryNodes, String first, String second) {
        List<String> list = StreamSupportUtils.stream(repositoryNodes)
                .filter(repositoryNode -> {
                    return ((RepositoryNode) repositoryNode).getText().equals(first)
                            || ((RepositoryNode) repositoryNode).getText().equals(second);
                })
                .map(repositoryNode -> ((RepositoryNode) repositoryNode).getText())
                .collect(Collectors.toList());
        return list.size() == 2 && list.get(0).equals(first) && list.get(1).equals(second);
    }

    private List<LocalRepoDescriptor> createLocalRepoDescriptors(RepoType repoType, int number) {
        return IntStream.range(1, number + 1)
                .mapToObj(index -> createLocalRepoDescriptor(repoType.getDisplayName() +
                        "-repo" + String.format("%03d", index), repoType))
                .collect(Collectors.toList());

    }

    private LocalRepoDescriptor createLocalRepoDescriptor(String repoKey, RepoType repoType) {
        LocalRepoDescriptor localRepoDescriptor = new LocalRepoDescriptor();
        localRepoDescriptor.setKey(repoKey);
        localRepoDescriptor.setType(repoType);
        return localRepoDescriptor;
    }

    private List<VirtualRepoDescriptor> createVirtualRepoDescriptors(RepoType repoType, int number) {
        return IntStream.range(1, number + 1)
                .mapToObj(index -> createVirtualRepoDescriptor(repoType.getDisplayName() +
                        "-virt-repo" + String.format("%03d", index), repoType))
                .collect(Collectors.toList());

    }

    private VirtualRepoDescriptor createVirtualRepoDescriptor(String repoKey, RepoType repoType) {
        VirtualRepoDescriptor virtualRepoDescriptor = new VirtualRepoDescriptor();
        virtualRepoDescriptor.setKey(repoKey);
        virtualRepoDescriptor.setType(repoType);
        return virtualRepoDescriptor;
    }

    private Map<Character, List<String>> createCharacterListMap(List<RepoDescriptor> repoDescriptors) {

        return StreamSupportUtils.stream(repoDescriptors)
                .map(RepoDescriptor::getKey)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.groupingBy(repoKey -> repoKey.toLowerCase().charAt(0)));
    }

}
