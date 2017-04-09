import java.util.Arrays;




public class main
{
	public static void main(String[] args)
	{
		DoctorData m = new DoctorData();
		String[][] results = m.getData("Psychiatric", "39.0016", "-77.0353");
		for(String[] doc : results)
		{
			System.out.println(Arrays.toString(doc) + "/n");
		}
	}
}