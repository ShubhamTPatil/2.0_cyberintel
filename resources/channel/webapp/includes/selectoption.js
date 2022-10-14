/*  Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
    and 6,430,608. Other Patents Pending.

    $File$, $Revision$, $Date$

     Transparent Cover with Select Options.

     @author Selvaraj Jegatheesan
     @version 2, 2009/03/23
*/

    function toggleFrameSelect(state) {
        var allFrames = document.getElementsByTagName("iframe");
        for(i=0;i<allFrames.length;i++) {        	if(window.frames[i].document != null){        		
        		var allSelects = window.frames[i].document.getElementsByTagName('select');
        		for(j=0;j<allSelects.length;j++){
        			allSelects[j].style.visibility = (state) ? "visible" : "hidden";
        		}        	}
        }
        var allFormSelects = document.getElementsByTagName('select');
            for(k=0;k<allFormSelects.length;k++){
                allFormSelects[k].style.visibility = (state) ? "visible" : "hidden";
            }
    }

    