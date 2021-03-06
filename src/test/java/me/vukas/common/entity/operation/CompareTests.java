package me.vukas.common.entity.operation;

import me.vukas.common.entity.IgnoredFields;
import me.vukas.common.entity.operation.model.ChildEntity;
import me.vukas.common.entity.operation.model.GrandChildEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CompareTests {
    private Compare compare;

    @Before
    public void buildNewCompareObject() {
        this.compare = new Compare.Builder().build();
    }

    @Test
    public void comparingNullsShouldReturnTrue() {
        assertThat(this.compare.compare(null, null), is(true));
    }

    @Test
    public void comparingNullToStringShouldReturnFalse() {
        assertThat(this.compare.compare(null, "null"), is(false));
    }

    @Test
    public void comparingStringToNullShouldReturnFalse() {
        assertThat(this.compare.compare("null", null), is(false));
    }

    @Test
    public void comparingEqualIntegersShouldReturnTrue() {
        assertThat(this.compare.compare(1, 1), is(true));
    }

    @Test
    public void comparingDifferentIntegersShouldReturnFalse() {
        assertThat(this.compare.compare(1, 2), is(false));
    }

    @Test
    public void comparingIntegerToLongShouldReturnFalse() {
        assertThat(this.compare.compare(1, 1L), is(false));
    }

    @Test
    public void comparingDoubleToIntegerShouldReturnFalse() {
        assertThat(this.compare.compare(1.0, 1), is(false));
    }

    @Test
    public void comparingEqualStringsShouldReturnTrue() {
        assertThat(this.compare.compare("one", "one"), is(true));
    }

    @Test
    public void comparingDifferentStringsShouldReturnTrue() {
        assertThat(this.compare.compare("one", "two"), is(false));
    }

    @Test
    public void comparingEmptyPrimitiveIntegerArraysShouldReturnTrue() {
        assertThat(this.compare.compare(new int[]{}, new int[]{}), is(true));
    }

    @Test
    public void comparingEqualPrimitiveIntegerArraysShouldReturnTrue() {
        assertThat(this.compare.compare(new int[]{1, 2, 3, 4}, new int[]{1, 2, 3, 4}), is(true));
    }

    @Test
    public void comparingEqualPrimitiveIntegerArraysWithRepeatingElementsShouldReturnTrue() {
        assertThat(this.compare.compare(new int[]{1, 2, 1, 3, 2, 4}, new int[]{1, 2, 1, 3, 2, 4}), is(true));
    }

    @Test
    public void comparingDifferentPrimitiveIntegerArraysWithSameLengthShouldReturnFalse() {
        assertThat(this.compare.compare(new int[]{1, 2, 3, 4}, new int[]{1, 2, 3, 5}), is(false));
    }

    @Test
    public void comparingDifferentPrimitiveIntegerArraysWithDifferentLengthShouldReturnFalse() {
        assertThat(this.compare.compare(new int[]{1, 2, 3, 4}, new int[]{1, 2, 3}), is(false));
    }

    @Test
    public void comparingEmptyIntegerArraysShouldReturnTrue() {
        assertThat(this.compare.compare(new Integer[]{}, new Integer[]{}), is(true));
    }

    @Test
    public void comparingEqualIntegerArraysShouldReturnTrue() {
        assertThat(this.compare.compare(new Integer[]{1, 2, 3, 4}, new Integer[]{1, 2, 3, 4}), is(true));
    }

    @Test
    public void comparingEqualIntegerArraysWithNullElementsShouldReturnTrue() {
        assertThat(this.compare.compare(new Integer[]{1, 2, 3, 4, null}, new Integer[]{1, 2, 3, 4, null}), is(true));
    }

    @Test
    public void comparingEqualIntegerArraysWithRepeatingElementsShouldReturnTrue() {
        assertThat(this.compare.compare(new Integer[]{1, 2, 1, 3, 2, 4}, new Integer[]{1, 2, 1, 3, 2, 4}), is(true));
    }

    @Test
    public void comparingDifferentIntegerArraysWithSameLengthShouldReturnFalse() {
        assertThat(this.compare.compare(new Integer[]{1, 2, 3, 4}, new Integer[]{1, 2, 3, 5}), is(false));
    }

    @Test
    public void comparingDifferentIntegerArraysWithDifferentLengthShouldReturnFalse() {
        assertThat(this.compare.compare(new Integer[]{1, 2, 3, 4}, new Integer[]{1, 2, 3}), is(false));
    }

    @Test
    public void comparingEmptyArrayListsShouldReturnTrue() {
        assertThat(this.compare.compare(Collections.emptyList(), Collections.emptyList()), is(true));
    }

    @Test
    public void comparingEqualIntegerArrayListsShouldReturnTrue() {
        assertThat(this.compare.compare(new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4)), new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4))), is(true));
    }

    @Test
    public void comparingEqualIntegerArrayListsWithNullElementsShouldReturnTrue() {
        assertThat(this.compare.compare(new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, null)), new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, null))), is(true));
    }

    @Test
    public void comparingEqualIntegerArrayListsWithRepeatingElementsShouldReturnTrue() {
        assertThat(this.compare.compare(new ArrayList<Integer>(Arrays.asList(1, 2, 1, 3, 2, 4)), new ArrayList<Integer>(Arrays.asList(1, 2, 1, 3, 2, 4))), is(true));
    }

    @Test
    public void comparingDifferentIntegerArrayListsWithSameLengthShouldReturnFalse() {
        assertThat(this.compare.compare(new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4)), new ArrayList<Integer>(Arrays.asList(1, 2, 3, 5))), is(false));
    }

    @Test
    public void comparingDifferentIntegerArrayListsWithDifferentLengthShouldReturnFalse() {
        assertThat(this.compare.compare(new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4)), new ArrayList<Integer>(Arrays.asList(1, 2, 3))), is(false));
    }

    @Test
    public void comparingEmptySetsShouldReturnTrue() {
        assertThat(this.compare.compare(Collections.emptySet(), Collections.emptySet()), is(true));
    }

    @Test
    public void comparingEqualIntegerSetsShouldReturnTrue() {
        assertThat(this.compare.compare(new HashSet<Integer>(Arrays.asList(1, 2, 3, 4)), new HashSet<Integer>(Arrays.asList(4, 3, 2, 1))), is(true));
    }

    @Test
    public void comparingEqualIntegerSetsWithNullElementsShouldReturnTrue() {
        assertThat(this.compare.compare(new HashSet<Integer>(Arrays.asList(1, 2, 3, null)), new HashSet<Integer>(Arrays.asList(null, 3, 2, 1))), is(true));
    }

    @Test
    public void comparingEqualIntegerSetsWithRepeatingElementsShouldReturnTrue() {
        assertThat(this.compare.compare(new HashSet<Integer>(Arrays.asList(1, 2, 3, 1, 3, 2, 4)), new HashSet<Integer>(Arrays.asList(4, 2, 3, 1, 3, 2, 1))), is(true));
    }

    @Test
    public void comparingDifferentIntegerSetsWithSameLengthShouldReturnFalse() {
        assertThat(this.compare.compare(new HashSet<Integer>(Arrays.asList(1, 2, 3, 4)), new HashSet<Integer>(Arrays.asList(1, 2, 3, 5))), is(false));
    }

    @Test
    public void comparingDifferentIntegerSetsWithDifferentLengthShouldReturnFalse() {
        assertThat(this.compare.compare(new HashSet<Integer>(Arrays.asList(1, 2, 3, 4)), new HashSet<Integer>(Arrays.asList(1, 2, 3))), is(false));
    }

    @Test
    public void comparingEqualUnorderedLinkedHashSetsShouldReturnTrue(){
        assertThat(this.compare.compare(new LinkedHashSet<Integer>(Arrays.asList(1, 2, 3, 4)), new LinkedHashSet<Integer>(Arrays.asList(2, 1, 4, 3))), is(true));
    }

    @Test
    public void comparingEqualUnorderedLinkedHashSetsWithRepeatingElementsShouldReturnTrue(){
        assertThat(this.compare.compare(new LinkedHashSet<Integer>(Arrays.asList(1, 2, 3, 4, 1, 1, 5, 2)), new LinkedHashSet<Integer>(Arrays.asList(2, 1, 4, 3, 2, 5, 1, 1))), is(true));
    }

    @Test
    public void comparingEmptyMapsShouldReturnTrue() {
        assertThat(this.compare.compare(Collections.emptyMap(), Collections.emptyMap()), is(true));
    }

    @Test
    public void comparingEqualIntegerMapsShouldReturnTrue() {
        Map<Integer, Integer> map1 = new HashMap<Integer, Integer>();
        map1.put(1, 2);
        map1.put(2, 3);
        map1.put(3, 4);
        map1.put(4, 5);
        Map<Integer, Integer> map2 = new HashMap<Integer, Integer>();
        map2.put(4, 5);
        map2.put(3, 4);
        map2.put(2, 3);
        map2.put(1, 2);
        assertThat(this.compare.compare(map1, map2), is(true));
    }

    @Test
    public void comparingEqualIntegerMapsWithNullElementsShouldReturnTrue() {
        Map<Integer, Integer> map1 = new HashMap<Integer, Integer>();
        map1.put(1, 2);
        map1.put(2, 3);
        map1.put(3, 4);
        map1.put(4, 5);
        map1.put(null, null);
        Map<Integer, Integer> map2 = new HashMap<Integer, Integer>();
        map2.put(null, null);
        map2.put(4, 5);
        map2.put(3, 4);
        map2.put(2, 3);
        map2.put(1, 2);
        assertThat(this.compare.compare(map1, map2), is(true));
    }

    @Test
    public void comparingEqualIntegerMapsWithRepeatingElementsShouldReturnTrue() {
        Map<Integer, Integer> map1 = new HashMap<Integer, Integer>();
        map1.put(1, 2);
        map1.put(2, 3);
        map1.put(3, 4);
        map1.put(1, 2);
        map1.put(3, 4);
        map1.put(4, 5);
        Map<Integer, Integer> map2 = new HashMap<Integer, Integer>();
        map2.put(4, 5);
        map2.put(3, 4);
        map2.put(1, 2);
        map2.put(3, 4);
        map2.put(2, 3);
        map2.put(1, 2);
        assertThat(this.compare.compare(map1, map2), is(true));
    }

    @Test
    public void comparingDifferentIntegerMapsWithSameLengthShouldReturnFalse() {
        Map<Integer, Integer> map1 = new HashMap<Integer, Integer>();
        map1.put(1, 2);
        map1.put(2, 3);
        map1.put(3, 4);
        map1.put(4, 5);
        map1.put(null, null);
        Map<Integer, Integer> map2 = new HashMap<Integer, Integer>();
        map2.put(null, null);
        map2.put(1, 2);
        map2.put(2, 3);
        map2.put(3, 4);
        map2.put(5, 6);
        assertThat(this.compare.compare(map1, map2), is(false));
    }

    @Test
    public void comparingDifferentIntegerMapsWithDifferentLengthShouldReturnFalse() {
        Map<Integer, Integer> map1 = new HashMap<Integer, Integer>();
        map1.put(1, 2);
        map1.put(2, 3);
        map1.put(3, 4);
        map1.put(4, 5);
        map1.put(null, null);
        Map<Integer, Integer> map2 = new HashMap<Integer, Integer>();
        map2.put(null, null);
        map2.put(1, 2);
        map2.put(2, 3);
        map2.put(3, 4);
        assertThat(this.compare.compare(map1, map2), is(false));
    }

    @Test
    public void comparingEmptyObjectGraphsShouldReturnTrue() {
        GrandChildEntity gce1 = new GrandChildEntity(false);
        GrandChildEntity gce2 = new GrandChildEntity(false);
        assertThat(this.compare.compare(gce1, gce2), is(true));
    }

    @Test
    public void comparingDifferentObjectGraphsShouldReturnFalse() {
        GrandChildEntity gce1 = new GrandChildEntity(1);
        GrandChildEntity gce2 = new GrandChildEntity(2);
        assertThat(this.compare.compare(gce1, gce2), is(false));
    }

    @Test
    public void comparingDifferentObjectGraphsWithIgnoredFieldsShouldReturnTrue() {
        GrandChildEntity gce1 = new GrandChildEntity(1);
        GrandChildEntity gce2 = new GrandChildEntity(1);
        gce1.setParent1(gce2);
        gce2.setParent2(gce1);
        gce1.setParent(gce2);
        gce2.setParent(null);

        Compare compare = new Compare.Builder()
                .ignoreFields(new IgnoredFields(GrandChildEntity.class, "parent1", "parent2").registerSuperclass(ChildEntity.class, "parent"))
                .build();

        assertThat(compare.compare(gce1, gce2), is(true));
    }
}
