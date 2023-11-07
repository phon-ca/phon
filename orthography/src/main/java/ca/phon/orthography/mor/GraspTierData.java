package ca.phon.orthography.mor;

import ca.phon.extensions.ExtendableObject;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A collection of Grasp entries for a tier
 */
public final class GraspTierData extends ExtendableObject implements Iterable<Grasp> {

    public static GraspTierData parseGraspTierData(String text) throws ParseException {
        final List<Grasp> grasps = new ArrayList<>();
        for(String block:text.trim().split("\\s")) {
            Grasp grasp = Grasp.fromString(block);
            grasps.add(grasp);
        }
        return new GraspTierData(grasps);
    }

    private final List<Grasp> grasps;

    public GraspTierData() {
        this(new ArrayList<>());
    }

    public GraspTierData(List<Grasp> grasps) {
        super();
        this.grasps = grasps;
    }

    public List<Grasp> getGrasps() {
        return Collections.unmodifiableList(grasps);
    }

    public int size() {
        return grasps.size();
    }

    public boolean isEmpty() {
        return grasps.isEmpty();
    }

    public boolean contains(Grasp grasp) {
        return grasps.contains(grasp);
    }

    public Grasp get(int index) {
        return grasps.get(index);
    }

    public int indexOf(Grasp gra) {
        return grasps.indexOf(gra);
    }

    @Override
    public String toString() {
        return grasps.stream().map(Grasp::toString).collect(Collectors.joining(" "));
    }

    @NotNull
    @Override
    public Iterator<Grasp> iterator() {
        return grasps.iterator();
    }

    @Override
    public void forEach(Consumer<? super Grasp> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<Grasp> spliterator() {
        return Iterable.super.spliterator();
    }

}
