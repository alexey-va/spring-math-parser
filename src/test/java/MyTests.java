import org.junit.Assert;
import org.junit.Test;
import ru.mfti.model.TokenManager;
import ru.mfti.model.util.ExpUtil;

import java.util.Optional;

public class MyTests {

    @Test
    public void testBrackets(){
        Optional<Integer> optionalI = ExpUtil.findBracketMismatch("( 1 + 2 { 3 -4 ) }");
        Assert.assertTrue(optionalI.isPresent());
        Assert.assertEquals(optionalI.get().intValue(), 15);
    }

}
