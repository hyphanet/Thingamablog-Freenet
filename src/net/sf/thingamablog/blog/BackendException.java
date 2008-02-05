/*
 * Created on Mar 29, 2004
 *
 * This file is part of Thingamablog. ( http://thingamablog.sf.net )
 *
 * Copyright (c) 2004, Bob Tantlinger All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */
package net.sf.thingamablog.blog;

/**
 * Signals that a BackendException of some sort occured
 * 
 * @author Bob Tantlinger 
 */
public class BackendException extends Exception
{

    /**
     * Constructs a BackendException
     */
    public BackendException()
    {
        super();
        
    }

    /**
     * @param message
     */
    public BackendException(String message)
    {
        super(message);
        
    }

    /**
     * @param message
     * @param cause
     */
    public BackendException(String message, Throwable cause)
    {
        super(message, cause);
        
    }

    /**
     * @param cause
     */
    public BackendException(Throwable cause)
    {
        super(cause);
        
    }

}
