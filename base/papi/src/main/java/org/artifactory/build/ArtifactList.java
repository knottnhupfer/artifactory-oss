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

package org.artifactory.build;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * @author Noam Y. Tenne
 */
public class ArtifactList implements List<Artifact> {

    private List<org.jfrog.build.api.Artifact> artifactList;

    ArtifactList(List<org.jfrog.build.api.Artifact> artifactList) {
        this.artifactList = artifactList;
    }

    @Override
    public int size() {
        return artifactList.size();
    }

    @Override
    public boolean isEmpty() {
        return artifactList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return artifactList.contains(((Artifact) o).getBuildArtifact());
    }

    @Override
    @Nonnull
    public Iterator<Artifact> iterator() {
        final Iterator<org.jfrog.build.api.Artifact> iterator = artifactList.iterator();
        return new Iterator<Artifact>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Artifact next() {
                return new Artifact(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    @Nonnull
    public Object[] toArray() {
        Object[] array = new Object[artifactList.size()];
        for (int i = 0; i < artifactList.size(); i++) {
            array[i] = new Artifact(artifactList.get(i));
        }
        return array;
    }

    @Override
    @Nonnull
    public <T> T[] toArray(@Nonnull T[] a) {
        Object[] array = toArray();
        for (int i = 0; i < array.length; i++) {
            a[i] = ((T) array[i]);
        }
        return a;
    }

    @Override
    public boolean add(Artifact artifact) {
        return artifactList.add(artifact.getBuildArtifact());
    }

    @Override
    public boolean remove(Object o) {
        return artifactList.remove(((Artifact) o).getBuildArtifact());
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        return artifactList.containsAll(c.stream()
                .map(input -> ((Artifact) input).getBuildArtifact())
                .collect(Collectors.toList()));
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends Artifact> c) {
        return artifactList.addAll(c.stream()
                .map(Artifact::getBuildArtifact)
                .collect(Collectors.toList()));
    }

    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends Artifact> c) {
        return artifactList.addAll(index,  c.stream()
                                           .map(Artifact::getBuildArtifact)
                                           .collect(Collectors.toList()));
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        return artifactList.removeAll(c.stream()
                .map(input -> ((Artifact) input).getBuildArtifact())
                .collect(Collectors.toList()));
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        return artifactList.retainAll(c.stream()
                .map(input -> ((Artifact) input).getBuildArtifact())
                .collect(Collectors.toList()));
    }

    @Override
    public void clear() {
        artifactList.clear();
    }

    @Override
    public Artifact get(int index) {
        return new Artifact(artifactList.get(index));
    }

    @Override
    public Artifact set(int index, Artifact element) {
        org.jfrog.build.api.Artifact artifact = artifactList.set(index, element.getBuildArtifact());
        if (artifact == null) {
            return null;
        }
        return new Artifact(artifact);
    }

    @Override
    public void add(int index, Artifact element) {
        artifactList.add(index, element.getBuildArtifact());
    }

    @Override
    public Artifact remove(int index) {
        org.jfrog.build.api.Artifact removed = artifactList.remove(index);
        if (removed == null) {
            return null;
        }
        return new Artifact(removed);
    }

    @Override
    public int indexOf(Object o) {
        return artifactList.indexOf(((Artifact) o).getBuildArtifact());
    }

    @Override
    public int lastIndexOf(Object o) {
        return artifactList.lastIndexOf(((Artifact) o).getBuildArtifact());
    }

    @Override
    public ListIterator<Artifact> listIterator() {
        return new ArtifactListIterator(artifactList.listIterator());
    }

    @Override
    public ListIterator<Artifact> listIterator(int index) {
        return new ArtifactListIterator(artifactList.listIterator(index));
    }

    @Override
    @Nonnull
    public List<Artifact> subList(int fromIndex, int toIndex) {
        return artifactList.subList(fromIndex, toIndex)
                .stream()
                .map(Artifact::new)
                .collect(Collectors.toList());
    }

    private class ArtifactListIterator implements ListIterator<Artifact> {

        private ListIterator<org.jfrog.build.api.Artifact> artifactListIterator;

        public ArtifactListIterator(ListIterator<org.jfrog.build.api.Artifact> artifactListIterator) {
            this.artifactListIterator = artifactListIterator;
        }

        @Override
        public boolean hasNext() {
            return artifactListIterator.hasNext();
        }

        @Override
        public Artifact next() {
            return new Artifact(artifactListIterator.next());
        }

        @Override
        public boolean hasPrevious() {
            return artifactListIterator.hasPrevious();
        }

        @Override
        public Artifact previous() {
            return new Artifact(artifactListIterator.previous());
        }

        @Override
        public int nextIndex() {
            return artifactListIterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return artifactListIterator.previousIndex();
        }

        @Override
        public void remove() {
            artifactListIterator.remove();
        }

        @Override
        public void set(Artifact artifact) {
            artifactListIterator.set(artifact.getBuildArtifact());
        }

        @Override
        public void add(Artifact artifact) {
            artifactListIterator.add(artifact.getBuildArtifact());
        }
    }
}
