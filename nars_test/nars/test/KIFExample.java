/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.test;

import nars.core.NAR;
import nars.core.NARState;
import nars.io.TextOutput;
import nars.io.kif.KIFInput;

/**
 *
 * @author me
 */
public class KIFExample {
    
 
    public static void main(String[] args) throws Exception {
        NAR n = new NAR();
        n.param.setSilenceLevel(99);
        
        KIFInput k = new KIFInput(n, "/home/me/sigma/KBs/Merge.kif");
        
        while (!k.isClosed()) {
            n.run(1);        
        }
        
        System.err.println("Processed operators: " + k.getKnownOperators());
        System.err.println("Unknown operators: " + k.getUnknownOperators());

        System.err.println(new NARState(n).measure());

        TextOutput t = new TextOutput(n, System.out);
        t.setErrors(true);
        t.setErrorStackTrace(true);

        n.run(100000);

        /*
        new TextInput(n, "$0.99;0.99$ <Human --> ?x>?");
        new TextInput(n, "$0.99;0.99$ <Human--> {?x}>?");
        new TextInput(n, "$0.99;0.99$ <?x --> Human>?");*/

        
        System.err.println(new NARState(n).measure());
    }
}
