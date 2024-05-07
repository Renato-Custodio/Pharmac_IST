import { Injectable, OnModuleInit } from '@nestjs/common';
import { GPSLocation, Medicine, Pharmacy, Stock } from '@prisma/client';
import { PrismaService } from 'src/core/prisma.service';
import { CreatePharmacyDto } from './dtos/create-pharmacy.dto';
import { CreateStockDto } from './dtos/create-stock.dto';

@Injectable()
export class PharmacyService implements OnModuleInit {
  constructor(private prismaService: PrismaService) {}
  async onModuleInit() {
    await this.prismaService.pharmacy.deleteMany();
    const pharmacyData = [];
    for (let i = 1; i <= 50; i++) {
      const pharmacy = {
        name: `Pharmacy${i}`,
        location: {
          lat: parseFloat((Math.random() * (100 - -200) + -200).toFixed(6)), // Random latitude between 100 and -200
          lng: parseFloat((Math.random() * (100 - -200) + -200).toFixed(6)), // Random longitude between 100 and -200
        },
        picture: '',
        rating: '',
        flagCount: Math.floor(Math.random() * 100) + 1, // Random quantity between 1 and 100
      };
      pharmacyData.push(pharmacy);
    }
    await this.prismaService.pharmacy.createMany({
      data: pharmacyData,
    });
  }

  async getpharmacies(pageNumber: number = 1): Promise<Pharmacy[]> {
    const skip = (pageNumber - 1) * 10;
    const pharmacies = await this.prismaService.pharmacy.findMany({
      skip: skip,
      take: 10,
    });
    return pharmacies;
  }

  async getAllMedicinesInPharmacy(
    pharmacyId: string,
    skip: number,
  ): Promise<Medicine[]> {
    return await this.prismaService.pharmacy
      .findUnique({
        where: { id: pharmacyId },
      })
      .medicines({
        skip: skip,
        take: 10,
      });
  }

  async getPharmacyInfo(pharmacyId: string): Promise<PharmacyInfo> {
    const pharmacy = await this.prismaService.pharmacy.findUnique({
      where: { id: pharmacyId },
      select: {
        name: true,
        location: true,
        picture: true,
      },
    });

    if (!pharmacy) {
      throw new Error('Pharmacy not found');
    }

    return {
      name: pharmacy.name,
      location: pharmacy.location,
      picture: pharmacy.picture,
    };
  }

  async createPharmacy(dto: CreatePharmacyDto): Promise<Pharmacy> {
    return await this.prismaService.pharmacy.create({
      data: {
        name: dto.name,
        location: dto.location,
        picture: '',
        rating: '',
        flagCount: 0,
      },
    });
  }

  async addMedicine(dto: CreateStockDto): Promise<Stock> {
    return await this.prismaService.stock.create({
      data: {
        medicineId: dto.medicineID,
        quantity: dto.quantity,
        pharmacyId: dto.pharmacyId,
      },
    });
  }

  async buyMedicine(pharmacyId: string, medicineId: string): Promise<Stock> {
    try {
      // Find the stock item based on pharmacyId and medicineId
      const existingStockItem = await this.prismaService.stock.findFirst({
        where: {
          pharmacyId: pharmacyId,
          medicineId: medicineId,
        },
      });

      // If the stock item exists, update its quantity
      if (existingStockItem) {
        const updatedStockItem = await this.prismaService.stock.update({
          where: {
            id: existingStockItem.id,
          },
          data: {
            quantity: existingStockItem.quantity - 1,
          },
        });
        console.log(
          `Stock quantity updated for medicine ${medicineId} in pharmacy ${pharmacyId}.`,
        );
        return updatedStockItem;
      } else {
        console.log(
          `Stock item not found for medicine ${medicineId} in pharmacy ${pharmacyId}.`,
        );
        return null;
      }
    } catch (error) {
      console.error('Error updating stock quantity:', error);
      throw error;
    }
  }
}

interface PharmacyInfo {
  name: string;
  location: GPSLocation;
  picture: string;
}
