package com.silverpeas.gallery;

import java.util.Comparator;

import com.silverpeas.gallery.model.PhotoDetail;

public class GSCSizeComparatorAsc implements Comparator
{
	static public GSCSizeComparatorAsc comparator = new GSCSizeComparatorAsc();

		
	public int compare(Object o1, Object o2)
	{
		PhotoDetail photo1 = (PhotoDetail) o1;
		PhotoDetail photo2 = (PhotoDetail) o2;
		
		int compareResult = new Long(photo1.getImageSize()).compareTo(new Long(photo2.getImageSize()));
		if (compareResult == 0) 
		{
			// les 2 photos on la même taille, comparer les dates
			compareResult = photo1.getCreationDate().compareTo(photo2.getCreationDate());
		}
		return compareResult;
	}

	public boolean equals(Object o)
	{
		return o == this;
	}
}
