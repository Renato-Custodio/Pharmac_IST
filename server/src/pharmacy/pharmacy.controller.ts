import { Body, Controller, Get, Param, Post, Put, Query } from '@nestjs/common';
import { PharmacyService } from './pharmacy.service';
import { Medicine, Pharmacy, Stock } from '@prisma/client';
import { CreatePharmacyDto } from './dtos/create-pharmacy.dto';
import { CreateStockDto } from './dtos/create-stock.dto';

@Controller('api/pharmacy')
export class PharmacyController {
  constructor(private readonly pharmacyService: PharmacyService) {}

  @Get('getAll')
  async findAll(@Query('pageNumber') pageNumber?: number): Promise<Pharmacy[]> {
    return await this.pharmacyService.getpharmacies(pageNumber);
  }

  @Get(':pharmacyId/medicines')
  async findQuery(
    @Param('pharmacyId') pharmacyId: string,
    @Query('pageNumber') pageNumber: number,
  ): Promise<Medicine[]> {
    return await this.pharmacyService.getAllMedicinesInPharmacy(
      pharmacyId,
      pageNumber,
    );
  }

  @Post('addMedicine')
  async addMedicine(@Body() createStockDto: CreateStockDto): Promise<Stock> {
    return await this.pharmacyService.addMedicine(createStockDto);
  }

  @Put(':pharmacyId/buy/:medicineId')
  async BuyMedicine(
    @Param('pharmacyId') pharmacyId: string,
    @Param('medicineId') medicineId: string,
  ): Promise<Stock> {
    return await this.pharmacyService.buyMedicine(pharmacyId, medicineId);
  }

  @Post()
  async createPharamacy(
    @Body() createPharmacyDto: CreatePharmacyDto,
  ): Promise<Pharmacy> {
    return await this.pharmacyService.createPharmacy(createPharmacyDto);
  }
}
