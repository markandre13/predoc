package algorithm;

// `sort' isn't a realy class but more C++ like namespace

public class sort
{
	public static void BubbleSort(Object a[], TCompare cmp)
	{
		BubbleSort(a, 0, a.length-1, cmp);
	}

	public static void BubbleSort(Object a[], int l, int r, TCompare cmp)
	{
		int i,j;
		for(j=r-1; j>l; j--) {
			boolean done = true;
			for(i=l; i<j; i++) {
				if (cmp.less(a[i+1], a[i])) {
					Object accu = a[i]; a[i] = a[i+1]; a[i+1] = accu;
					done = false;
				}
			}
			if (done)
				break;
		}
	}

	public static void BubbleSort(int a[])
	{
		BubbleSort(a, 0, a.length);
	}

	public static void BubbleSort(int a[], int l, int r)
	{
		int i,j;
		for(j=r-1; j>l; j--) {
			for(i=l; i<j; i++) {
				if (a[i+1]<a[i]) {
					int accu = a[i]; a[i] = a[i+1]; a[i+1] = accu;
				}
			}
		}
	}
}
