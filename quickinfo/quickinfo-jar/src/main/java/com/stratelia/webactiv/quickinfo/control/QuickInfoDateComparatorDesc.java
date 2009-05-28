package com.stratelia.webactiv.quickinfo.control;

import java.util.Comparator;
import com.stratelia.webactiv.util.publication.model.*;

public class QuickInfoDateComparatorDesc implements Comparator
{
	static public QuickInfoDateComparatorDesc comparator = new QuickInfoDateComparatorDesc();

	/**
	 * This result is reversed as we want a descending sort.
	 */
	public int compare(Object o1, Object o2)
	{
		PublicationDetail qI1 = (PublicationDetail) o1;
		PublicationDetail qI2 = (PublicationDetail) o2;

		int compareResult = qI1.getUpdateDate().compareTo(qI2.getUpdateDate());

		return 0-compareResult;
	}

	/**
	 * This comparator equals self only.
	 * 
	 * Use the shared comparator QuickInfoDateComparatorDesc.comparator
	 * if multiples comparators are used.
	 */
	public boolean equals(Object o)
	{
		return o == this;
	}
}