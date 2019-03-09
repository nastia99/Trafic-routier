package controller;

import java.util.ArrayList;
import java.util.List;

public final class Compute {

	private Compute() {}
	
	public static int[] intersect(int[] tab1, int[] tab2) {
		List<Integer> fuzeList = new ArrayList<>();
		if (tab1 != null && tab2 != null) {
			for (int i : tab1) {
				for (int j : tab2) {
					if (i == j) {
						fuzeList.add(i);
					}
				}
			}
		}
		int [] fuzeTab = new int[fuzeList.size()];
		for (int i = 0; i < fuzeTab.length; i++) {
			fuzeTab[i] = fuzeList.get(i);
		}
		return fuzeTab;
	}	
}