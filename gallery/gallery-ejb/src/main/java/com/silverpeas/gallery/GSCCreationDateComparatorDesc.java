package com.silverpeas.gallery;

import java.util.Comparator;

import com.silverpeas.gallery.model.PhotoDetail;

public class GSCCreationDateComparatorDesc implements Comparator
{
	static public GSCCreationDateComparatorDesc comparator = new GSCCreationDateComparatorDesc();

		
	public int compare(Object o1, Object o2)
	{
		PhotoDetail photo1 = (PhotoDetail) o1;
		PhotoDetail photo2 = (PhotoDetail) o2;
		
		int compareResult = photo1.getCreationDate().compareTo(photo2.getCreationDate());
		if (compareResult == 0) 
		{
			// les 2 photos on été créée à la même date, comparer les Id
			compareResult = photo1.getPhotoPK().getId().compareTo(photo2.getPhotoPK().getId());
		}
		return 0-compareResult;
	}

	public boolean equals(Object o)
	{
		return o == this;
	}
}
