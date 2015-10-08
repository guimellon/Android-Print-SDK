/*****************************************************
 *
 * OrderPricingAdaptor.java
 *
 *
 * Modified MIT License
 *
 * Copyright (c) 2010-2015 Kite Tech Ltd. https://www.kite.ly
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The software MAY ONLY be used with the Kite Tech Ltd platform and MAY NOT be modified
 * to be used with any competitor platforms. This means the software MAY NOT be modified 
 * to place orders with any competitors to Kite Tech Ltd, all orders MUST go through the
 * Kite Tech Ltd platform servers. 
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 *****************************************************/

///// Package Declaration /////

package ly.kite.checkout;


///// Import(s) /////

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ly.kite.R;
import ly.kite.pricing.OrderPricing;
import ly.kite.catalogue.MultipleCurrencyAmount;
import ly.kite.catalogue.SingleCurrencyAmount;


///// Class Declaration /////

/*****************************************************
 *
 * An adaptor for the image sources.
 *
 *****************************************************/
public class OrderPricingAdaptor extends BaseAdapter
  {
  ////////// Static Constant(s) //////////

  @SuppressWarnings( "unused" )
  private static final String  LOG_TAG = "OrderPricingAdaptor";


  ////////// Static Variable(s) //////////


  ////////// Member Variable(s) //////////

  private Context          mContext;
  private OrderPricing     mPricing;

  private LayoutInflater   mLayoutInflator;

  private ArrayList<Item>  mItemList;


  ////////// Static Initialiser(s) //////////


  ////////// Static Method(s) //////////


  ////////// Constructor(s) //////////

  public OrderPricingAdaptor( Context context, OrderPricing pricing )
    {
    mContext        = context;
    mPricing        = pricing;

    mLayoutInflator = LayoutInflater.from( context );


    // Create the item list

    mItemList = new ArrayList<>();

    if ( pricing == null ) return;


    Locale defaultLocale = Locale.getDefault();

    ///// Line items

    List<OrderPricing.LineItem> lineItemList = pricing.getLineItems();

    for ( OrderPricing.LineItem lineItem : lineItemList )
      {
      String description = lineItem.getDescription();

      MultipleCurrencyAmount itemCost = lineItem.getProductCost();

      String itemCostString = itemCost.getDefaultDisplayAmountWithFallback();

      mItemList.add( new Item( description, itemCostString, false ) );
      }


    ///// Shipping

    MultipleCurrencyAmount shippingCost = pricing.getTotalShippingCost();
    SingleCurrencyAmount   shippingCostInSingleCurrency;
    BigDecimal             shippingCostAmount;
    String                 shippingCostString;

    if ( shippingCost != null &&
         ( shippingCostInSingleCurrency = shippingCost.getAmountWithFallback( defaultLocale )) != null &&
         ( shippingCostAmount = shippingCostInSingleCurrency.getAmount() ).compareTo( BigDecimal.ZERO ) > 0 )
      {
      shippingCostString = shippingCostInSingleCurrency.getDisplayAmountForLocale( defaultLocale );
      }
    else
      {
      shippingCostString = mContext.getString( R.string.Free );
      }

    mItemList.add( new Item( mContext.getString( R.string.Shipping ), shippingCostString, false ) );


    ///// Promo code

    MultipleCurrencyAmount promoDiscount = pricing.getPromoCodeDiscount();

    if ( promoDiscount != null )
      {
      SingleCurrencyAmount promoDiscountInSingleCurrency = promoDiscount.getAmountWithFallback( defaultLocale );

      BigDecimal promoDiscountAmount;

      if ( promoDiscountInSingleCurrency != null &&
           ( promoDiscountAmount = promoDiscountInSingleCurrency.getAmount() ).compareTo( BigDecimal.ZERO ) > 0 )
        {
        mItemList.add( new Item( mContext.getString( R.string.Promotional_Discount ), promoDiscountInSingleCurrency.getDisplayAmountForLocale( defaultLocale ), false ) );
        }
      }


    ///// Total

    MultipleCurrencyAmount totalCost = pricing.getTotalCost();

    if ( totalCost != null )
      {
      SingleCurrencyAmount totalCostInSingleCurrency = totalCost.getAmountWithFallback( defaultLocale );

      mItemList.add( new Item( mContext.getString( R.string.Total ), totalCostInSingleCurrency.getDisplayAmountForLocale( defaultLocale ), true ) );
      }


    }


  ////////// BaseAdapter Method(s) //////////

  /*****************************************************
   *
   * Returns the number of product items.
   *
   *****************************************************/
  @Override
  public int getCount()
    {
    return ( mItemList.size() );
    }


  /*****************************************************
   *
   * Returns the product item at the requested position.
   *
   *****************************************************/
  @Override
  public Object getItem( int position )
    {
    return ( mItemList.get( position ) );
    }


  /*****************************************************
   *
   * Returns an id for the product item at the requested
   * position.
   *
   *****************************************************/
  @Override
  public long getItemId( int position )
    {
    return ( 0 );
    }


  /*****************************************************
   *
   * Returns the view for the product item at the requested
   * position.
   *
   *****************************************************/
  @Override
  public View getView( int position, View convertView, ViewGroup parent )
    {
    // Either re-use the convert view, or create a new one.

    Object          tagObject;
    View            view;
    ViewReferences  viewReferences;

    if ( convertView != null &&
            ( tagObject = convertView.getTag() ) != null &&
            ( tagObject instanceof ViewReferences ) )
      {
      view           = convertView;
      viewReferences = (ViewReferences)tagObject;
      }
    else
      {
      view                               = mLayoutInflator.inflate( R.layout.list_item_order_pricing, null );
      viewReferences                     = new ViewReferences();
      viewReferences.descriptionTextView = (TextView)view.findViewById( R.id.description_text_view );
      viewReferences.amountTextView      = (TextView)view.findViewById( R.id.amount_text_view );

      view.setTag( viewReferences );
      }


    // Set up the view

    Item item = (Item)getItem( position );

    viewReferences.descriptionTextView.setText( item.description );
    viewReferences.amountTextView.setText( item.amount );

    if ( item.isBold )
      {
      viewReferences.descriptionTextView.setTypeface( Typeface.DEFAULT_BOLD );
      viewReferences.amountTextView.setTypeface( Typeface.DEFAULT_BOLD );
      }
    else
      {
      viewReferences.descriptionTextView.setTypeface( Typeface.DEFAULT );
      viewReferences.amountTextView.setTypeface( Typeface.DEFAULT );
      }


    return ( view );
    }


  ////////// Inner Class(es) //////////

  /*****************************************************
   *
   * A row item.
   *
   *****************************************************/
  private class Item
    {
    String   description;
    String   amount;
    boolean  isBold;

    Item( String description, String amount, boolean isBold )
      {
      this.description = description;
      this.amount      = amount;
      this.isBold      = isBold;
      }
    }


  /*****************************************************
   *
   * References to views within the layout.
   *
   *****************************************************/
  private class ViewReferences
    {
    TextView  descriptionTextView;
    TextView  amountTextView;
    }

  }

